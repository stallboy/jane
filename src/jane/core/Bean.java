package jane.core;

import java.io.Serializable;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import jane.core.SContext.Safe;

/**
 * bean的基类(抽象类)
 * <p>
 * 模版类型B表示bean的实际类型<br>
 * 一个Bean及其子类的实例不能同时由多个线程同时访问
 */
public abstract class Bean<B extends Bean<B>> implements Comparable<B>, Cloneable, Serializable, WriteRequest
{
	private static final long serialVersionUID = 1L;
	private transient int	  _serial;				// 用作协议时的序列号;也用于存储时的状态(0:未存储,1:已存储但未修改,2:已存储且已修改)

	/**
	 * 获取协议的序列号
	 */
	public final int serial()
	{
		return _serial;
	}

	/**
	 * 设置协议的序列号
	 */
	final void serial(int s)
	{
		_serial = s;
	}

	/**
	 * 获取存储标记
	 * <p>
	 * 如果已保存在数据库cache中,则一直持有此标记,只有用户新建的对象没有此标记<br>
	 * 有此标记的bean不能被其它的记录共享保存,以免出现意外的修改
	 */
	public final boolean stored()
	{
		return _serial > 0;
	}

	/**
	 * 获取修改标记
	 * <p>
	 * 作为数据库记录时有效. 标记此记录是否在缓存中有修改(和数据库存储的记录有差异),即脏记录
	 */
	public final boolean modified()
	{
		return _serial == 2;
	}

	/**
	 * 设置存储状态
	 * <p>
	 * @param saveState 当此记录在事务内有修改时,会设置为2以提示数据库缓存系统在合适的时机提交到数据库存储系统
	 */
	final void setSaveState(int saveState)
	{
		_serial = saveState;
	}

	/**
	 * bean的类型值
	 * <p>
	 * 用于区别于其它bean的类型值. 标准的bean子类必须大于0且不能重复, 0仅用于RawBean等特定类型
	 */
	public abstract int type();

	/**
	 * bean的类型名
	 */
	public abstract String typeName();

	/**
	 * 获取此bean类唯一的stub对象
	 */
	public abstract B stub();

	/**
	 * 创建一个新的bean实例
	 * <p>
	 * 子类的实现一般是new B(),返回对象的所有字段只有初始的默认值
	 */
	public abstract B create();

	/**
	 * 从另一个bean赋值到自身
	 * @param b
	 */
	public void assign(B b)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 从另一个safe bean赋值到自身
	 */
	@SuppressWarnings("deprecation")
	public void assign(Safe<B> b)
	{
		assign(b.unsafe());
	}

	/**
	 * bean的初始预计序列化长度
	 * <p>
	 * 用于序列化此bean前预留的空间大小(字节). 子类应该实现这个方法返回合适的值,默认只有16字节
	 */
	@SuppressWarnings("static-method")
	public int initSize()
	{
		return 16;
	}

	/**
	 * bean的最大序列化长度
	 * <p>
	 * 用于限制网络接收bean时的限制,避免对方恶意夸大长度攻击服务器的内存分配. 子类应该实现这个方法返回合适的值,默认是最大值表示不受限
	 */
	@SuppressWarnings("static-method")
	public int maxSize()
	{
		return Integer.MAX_VALUE;
	}

	/**
	 * 重置bean的所有字段为初始的默认值
	 */
	public abstract void reset();

	/**
	 * 序列化此bean到os中(用于数据库的记录)
	 * @return 必须是参数os
	 */
	public abstract OctetsStream marshal(OctetsStream os);

	/**
	 * 从os中反序列化到此bean中(用于数据库的记录)
	 * <p>
	 * 如果反序列化失败则抛出MarshalException
	 * @return 必须是参数os
	 */
	public abstract OctetsStream unmarshal(OctetsStream os) throws MarshalException;

	/**
	 * 序列化此bean到os中(用于网络协议)
	 * <p>
	 * 默认等同于数据库记录的序列化
	 * @return 必须是参数os
	 */
	public OctetsStream marshalProtocol(OctetsStream os)
	{
		return marshal(os);
	}

	/**
	 * 从os中反序列化到此bean中(用于网络协议)
	 * <p>
	 * 如果反序列化失败则抛出MarshalException<br>
	 * 默认等同于数据库记录的反序列化
	 * @return 必须是参数os
	 */
	public OctetsStream unmarshalProtocol(OctetsStream os) throws MarshalException
	{
		return unmarshal(os);
	}

	@Override
	public abstract B clone();

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);

	@Override
	public int compareTo(B b)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取自身的安全封装(在事务中支持异常回滚)
	 * @param parent
	 */
	public Safe<B> safe(Safe<?> parent)
	{
		throw new UnsupportedOperationException();
	}

	public Safe<B> safe()
	{
		return safe(null);
	}

	@Override
	public final Object getMessage()
	{
		return this;
	}

	@Override
	public final WriteFuture getFuture()
	{
		return DefaultWriteRequest.UNUSED_FUTURE;
	}
}
