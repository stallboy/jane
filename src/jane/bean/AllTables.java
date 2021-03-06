// This file is generated by genbeans tool. Do NOT edit it! @formatter:off
package jane.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import jane.core.Bean;
import jane.core.DBManager;
import jane.core.Octets;
import jane.core.Table;
import jane.core.TableBase;
import jane.core.TableLong;
import jane.core.map.IntHashMap;

/** 全部的数据库表的注册和使用类(自动生成的静态类) */
public final class AllTables
{
	private AllTables() {}
	private static final DBManager _dbm = DBManager.instance();
	/**
	 * 注册全部的数据库表<p>
	 * 用于初始化和注册下面的全部静态成员(保持和AllBeans.register一致的用法), 并启动提交线程<br>
	 * 调用前要先初始化数据库管理器: DBManager.instance().startup(...)
	 */
	public static void register() { _dbm.startCommitThread(); }

	/**
	 * 数据库表定义. key类型只能是32/64位整数/浮点数或字符串/binary类型或bean类型, id类型表示优化的非负数long类型
	 */
	public static final TableLong<TestType, TestType.Safe> TestTable = _dbm.<TestType, TestType.Safe>openTable(1, "TestTable", "test", 65536, TestType.BEAN_STUB);
	/**
	 * value类型必须是bean定义的类型
	 */
	public static final Table<TestKeyBean, TestBean, TestBean.Safe> BeanTable = _dbm.<TestKeyBean, TestBean, TestBean.Safe>openTable(2, "BeanTable", "bean", 65536, TestKeyBean.BEAN_STUB, TestBean.BEAN_STUB);
	/**
	 * 没有定义id的是内存表. 注意表名和key类型的对应关系是不能改变的
	 */
	public static final Table<Octets, TestEmpty, TestEmpty.Safe> OctetsTable = _dbm.<Octets, TestEmpty, TestEmpty.Safe>openTable(0, "OctetsTable", "bean", 1000, null, null);
	/**
	 * 用于测试数据库的表
	 */
	public static final TableLong<TestBean, TestBean.Safe> Benchmark = _dbm.<TestBean, TestBean.Safe>openTable(3, "Benchmark", "bench", 50000, TestBean.BEAN_STUB);

	/**
	 * 以下内部类可以单独使用,避免初始化前面的表对象
	 */
	public static final class MetaTable
	{
		private static final ArrayList<MetaTable> metaList = new ArrayList<>(4);
		private static final IntHashMap<MetaTable> idMetas = new IntHashMap<>(4 * 2);
		private static final HashMap<String, MetaTable> nameMetas = new HashMap<>(4 * 2);

		public final TableBase<?> table;
		public final Object keyBeanStub; // Class<?> or Bean<?>
		public final Bean<?> valueBeanStub;

		private MetaTable(TableBase<?> tbl, Object kbs, Bean<?> vbs)
		{
			table = tbl;
			keyBeanStub = kbs;
			valueBeanStub = vbs;
		}

		static
		{
			MetaTable mt;
			metaList.add(mt = new MetaTable(TestTable, Long.class, TestType.BEAN_STUB));
			idMetas.put(1, mt);
			nameMetas.put("TestTable", mt);
			metaList.add(mt = new MetaTable(BeanTable, TestKeyBean.BEAN_STUB, TestBean.BEAN_STUB));
			idMetas.put(2, mt);
			nameMetas.put("BeanTable", mt);
			metaList.add(mt = new MetaTable(OctetsTable, Octets.class, TestEmpty.BEAN_STUB));
			idMetas.put(0, mt);
			nameMetas.put("OctetsTable", mt);
			metaList.add(mt = new MetaTable(Benchmark, Long.class, TestBean.BEAN_STUB));
			idMetas.put(3, mt);
			nameMetas.put("Benchmark", mt);
		}

		public static MetaTable get(int tableId)
		{
			return idMetas.get(tableId);
		}

		public static MetaTable get(String tableName)
		{
			return nameMetas.get(tableName);
		}

		public static void foreach(Consumer<MetaTable> consumer)
		{
			metaList.forEach(consumer);
		}
	}
}
