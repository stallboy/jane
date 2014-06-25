package jane.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import jane.core.SContext.Record;
import jane.core.SContext.Safe;

/**
 * 通用key类型的数据库表类
 */
public final class Table<K, V extends Bean<V>, S extends Safe<V>>
{
	private static final List<Table<?, ?, ?>> _tables = new ArrayList<Table<?, ?, ?>>(16); // 所有的通用key类型的表列表
	private final String                      _tableName;                                 // 表名
	private final Storage.Table<K, V>         _stoTable;                                  // 存储引擎的表对象
	private final Map<K, V>                   _cache;                                     // 读缓存. 有大小限制,溢出自动清理
	private final ConcurrentMap<K, V>         _cacheMod;                                  // 写缓存. 不会溢出,保存到数据库存储引擎后清理
	private final V                           _deleted;                                   // 表示已删除的value. 同存根bean
	private final int                         _lockId;                                    // 当前表的锁ID. 即锁名的hash值,一般和记录key的hash值计算得出记录的lockId

	/**
	 * 尝试依次加锁并保存全部表已修改的记录
	 * <p>
	 * @param counts 长度必须>=2,用于保存两个统计值,前一个是保存前所有修改的记录数,后一个是保存后的剩余记录数
	 */
	static void trySaveModifiedAll(long[] counts)
	{
		long m = counts[0], n = counts[1];
		for(Table<?, ?, ?> table : _tables)
		{
			try
			{
				table.trySaveModified(counts);
			}
			catch(Throwable e)
			{
				Log.log.error("db-commit thread exception(trySaveModified:" + table.getTableName() + "):", e);
			}
			finally
			{
				m += counts[0];
				n += counts[1];
			}
		}
		counts[0] = m;
		counts[1] = n;
	}

	/**
	 * 在所有事务暂停的情况下直接依次保存全部表已修改的记录
	 */
	static int saveModifiedAll()
	{
		int m = 0;
		for(Table<?, ?, ?> table : _tables)
		{
			try
			{
				m += table.saveModified();
			}
			catch(Throwable e)
			{
				Log.log.error("db-commit thread exception(saveModified:" + table.getTableName() + "):", e);
			}
		}
		return m;
	}

	/**
	 * 创建一个数据库表
	 * @param tableName 表名
	 * @param stoTable 存储引擎的表对象. null表示此表是内存表
	 * @param lockName 此表关联的锁名
	 * @param cacheSize 此表的读缓存记录数量上限. 如果是内存表则表示超过此上限则会自动丢弃
	 * @param stubV 记录value的存根对象,不要用于记录有用的数据. 这里只用于标记删除的字段,如果为null则表示此表是内存表
	 */
	Table(int tableId, String tableName, Storage.Table<K, V> stoTable, String lockName, int cacheSize, V stubV)
	{
		_tableName = tableName;
		_stoTable = stoTable;
		_lockId = (lockName != null && !(lockName = lockName.trim()).isEmpty() ? lockName.hashCode() : tableId) * 0x9e3779b1;
		_cache = Util.newLRUConcurrentHashMap(cacheSize);
		_cacheMod = (stoTable != null ? Util.<K, V>newConcurrentHashMap() : null);
		_deleted = stubV;
		if(stoTable != null) _tables.add(this);
	}

	/**
	 * 获取数据库表名
	 */
	public String getTableName()
	{
		return _tableName;
	}

	/**
	 * 根据记录的key获取锁的ID(lockId)
	 * <p>
	 * 用于事务的加锁({@link Procedure#lock})
	 */
	public int lockId(K k)
	{
		return _lockId ^ k.hashCode();
	}

	/**
	 * 尝试依次加锁并保存此表已修改的记录
	 * <p>
	 * @param counts 长度必须>=2,用于保存两个统计值,前一个是保存前所有修改的记录数,后一个是保存后的剩余记录数
	 */
	private void trySaveModified(long[] counts)
	{
		counts[0] = _cacheMod.size();
		try
		{
			for(K k : _cacheMod.keySet())
			{
				Lock lock = Procedure.tryLock(lockId(k));
				if(lock != null)
				{
					try
					{
						V v = _cacheMod.get(k);
						if(v == _deleted)
							_stoTable.remove(k);
						else
						{
							v.setSaveState(1);
							_stoTable.put(k, v);
						}
						_cacheMod.remove(k, v);
					}
					finally
					{
						lock.unlock();
					}
				}
			}
		}
		finally
		{
			counts[1] = _cacheMod.size();
		}
	}

	/**
	 * 在所有事务暂停的情况下直接依次保存此表已修改的记录
	 */
	private int saveModified()
	{
		for(Entry<K, V> e : _cacheMod.entrySet())
		{
			K k = e.getKey();
			V v = e.getValue();
			if(v == _deleted)
				_stoTable.remove(k);
			else
			{
				v.setSaveState(1);
				_stoTable.put(k, v);
			}
		}
		int m = _cacheMod.size();
		_cacheMod.clear();
		return m;
	}

	/**
	 * 根据记录的key获取value
	 * <p>
	 * 会自动添加到读cache中<br>
	 * 必须在事务中已加锁的状态下调用此方法
	 */
	public V get(K k)
	{
		V v = _cache.get(k);
		if(v != null) return v;
		if(_cacheMod == null) return null;
		v = _cacheMod.get(k);
		if(v != null)
		{
			if(v == _deleted) return null;
			_cache.put(k, v);
			return v;
		}
		v = _stoTable.get(k);
		if(v != null)
		{
			v.setSaveState(1);
			_cache.put(k, v);
		}
		return v;
	}

	/**
	 * 同get,但增加的安全封装,可回滚修改
	 */
	public S getSafe(K k)
	{
		V v = get(k);
		return v != null ? SContext.current().addRecord(this, k, v) : null;
	}

	/**
	 * 根据记录的key获取value
	 * <p>
	 * 不会自动添加到读cache中<br>
	 * 必须在事务中已加锁的状态下调用此方法
	 */
	public V getNoCache(K k)
	{
		V v = _cache.get(k);
		if(v != null) return v;
		if(_cacheMod == null) return null;
		v = _cacheMod.get(k);
		if(v != null)
		{
			if(v == _deleted) return null;
			return v;
		}
		return _stoTable.get(k);
	}

	/**
	 * 同getNoCache,但增加的安全封装,可回滚修改
	 */
	public S getNoCacheSafe(K k)
	{
		V v = getNoCache(k);
		return v != null ? SContext.current().addRecord(this, k, v) : null;
	}

	/**
	 * 根据记录的key获取value
	 * <p>
	 * 只在读和写cache中获取<br>
	 * 必须在事务中已加锁的状态下调用此方法
	 */
	public V getCache(K k)
	{
		V v = _cache.get(k);
		if(v != null) return v;
		if(_cacheMod == null) return null;
		v = _cacheMod.get(k);
		return v != null && v != _deleted ? v : null;
	}

	/**
	 * 同getCache,但增加的安全封装,可回滚修改
	 */
	public S getCacheSafe(K k)
	{
		V v = getCache(k);
		return v != null ? SContext.current().addRecord(this, k, v) : null;
	}

	/**
	 * 标记记录已修改的状态
	 * <p>
	 * 必须在事务中已加锁的状态下调用此方法
	 * @param v 必须是get获取到的对象引用. 如果不是,则应该调用put方法
	 */
	public void modify(K k, V v)
	{
		if(!v.modified())
		{
			if(_cacheMod != null)
			{
				V vOld = _cacheMod.put(k, v);
				if(vOld == null)
					DBManager.instance().incModCount();
				else if(vOld != v)
				{
					_cacheMod.put(k, vOld);
					throw new IllegalStateException("modify unmatched record: t=" + _tableName +
					        ",k=" + k + ",vOld=" + vOld + ",v=" + v);
				}
			}
			v.setSaveState(2);
		}
	}

	@SuppressWarnings("unchecked")
	void modifySafe(Object k, Object v)
	{
		modify((K)k, (V)v);
	}

	/**
	 * 根据记录的key保存value
	 * <p>
	 * 必须在事务中已加锁的状态下调用此方法
	 * @param v 如果是get获取到的对象引用,可调用modify来提高性能
	 */
	public void put(K k, V v)
	{
		V vOld = _cache.put(k, v);
		if(vOld == v)
			modify(k, v);
		else
		{
			if(!v.stored())
			{
				if(_cacheMod != null)
				{
					vOld = _cacheMod.put(k, v);
					if(vOld == null)
					    DBManager.instance().incModCount();
				}
				v.setSaveState(2);
			}
			else
			{
				if(vOld != null)
					_cache.put(k, vOld);
				else
					_cache.remove(k);
				throw new IllegalStateException("put shared record: t=" + _tableName +
				        ",k=" + k + ",vOld=" + vOld + ",v=" + v);
			}
		}
	}

	/**
	 * 同put,但增加的安全封装,可回滚修改
	 */
	public void putSafe(final K k, V v)
	{
		if(v.stored())
		    throw new IllegalStateException("put shared record: t=" + _tableName + ",k=" + k + ",v=" + v);
		final V vOld = getNoCache(k);
		SContext.current().addOnRollback(new Runnable()
		{
			@Override
			public void run()
			{
				if(vOld != null)
				{
					vOld.setSaveState(0);
					put(k, vOld);
				}
				else
					remove(k);
			}
		});
		put(k, v);
	}

	public void putSafe(K k, S s)
	{
		putSafe(k, s.unsafe());
		s.record(new Record<K, V, S>(this, k, s));
	}

	/**
	 * 根据记录的key删除记录
	 * <p>
	 * 必须在事务中已加锁的状态下调用此方法
	 */
	public void remove(K k)
	{
		_cache.remove(k);
		if(_cacheMod != null && _cacheMod.put(k, _deleted) == null)
		    DBManager.instance().incModCount();
	}

	/**
	 * 同remove,但增加的安全封装,可回滚修改
	 */
	public void removeSafe(final K k)
	{
		final V vOld = getNoCache(k);
		if(vOld == null) return;
		SContext.current().addOnRollback(new Runnable()
		{
			@Override
			public void run()
			{
				vOld.setSaveState(0);
				put(k, vOld);
			}
		});
		remove(k);
	}

	/**
	 * 只在读cache中遍历此表的所有记录
	 * <p>
	 * 遍历时注意先根据记录的key获取锁再调用get获得其value, 必须在事务中调用此方法<br>
	 * 注意此遍历方法是无序的
	 * @param handler 遍历过程中返回false可中断遍历
	 */
	public boolean walkCache(Storage.WalkHandler<K> handler)
	{
		for(K k : _cache.keySet())
			if(!handler.onWalk(k)) return false;
		return true;
	}

	/**
	 * 按记录key的顺序遍历此表的所有记录
	 * <p>
	 * 遍历时注意先根据记录的key获取锁再调用get获得其value, 必须在事务中调用此方法
	 * @param handler 遍历过程中返回false可中断遍历
	 * @param from 需要遍历的最小key. null表示最小值
	 * @param to 需要遍历的最大key. null表示最大值
	 * @param inclusive 遍历是否包含from和to的key
	 * @param reverse 是否按反序遍历
	 */
	public boolean walk(Storage.WalkHandler<K> handler, K from, K to, boolean inclusive, boolean reverse)
	{
		if(_stoTable != null)
		    return _stoTable.walk(handler, from, to, inclusive, reverse);
		return walkCache(handler);
	}
}
