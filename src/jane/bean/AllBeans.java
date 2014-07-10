// This file is generated by genbeans tool. Do NOT edit it! @formatter:off
package jane.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jane.core.Bean;
import jane.core.BeanHandler;
import jane.core.IntMap;

/** 全部beans的注册(自动生成的静态类) */
public final class AllBeans
{
	private AllBeans() {}

	/** 获取全部的bean实例 */
	public static Collection<Bean<?>> getAllBeans()
	{
		List<Bean<?>> r = new ArrayList<Bean<?>>(4);
		r.add(TestBean.BEAN_STUB);
		r.add(TestType.BEAN_STUB);
		r.add(TestEmpty.BEAN_STUB);
		r.add(TestRpcBean.BEAN_STUB);
		return r;
	}

	public static IntMap<BeanHandler<?>> getTestClientHandlers()
	{
		IntMap<BeanHandler<?>> r = new IntMap<BeanHandler<?>>(3 * 4);
		r.put(1, new jane.handler.testclient.TestBeanHandler());
		r.put(2, new jane.handler.testclient.TestTypeHandler());
		r.put(4, new jane.handler.testclient.TestRpcBeanHandler());
		return r;
	}

	public static IntMap<BeanHandler<?>> getTestServerHandlers()
	{
		IntMap<BeanHandler<?>> r = new IntMap<BeanHandler<?>>(4 * 4);
		r.put(1, new jane.handler.testserver.TestBeanHandler());
		r.put(2, new jane.handler.testserver.TestTypeHandler());
		r.put(3, new jane.handler.testserver.TestEmptyHandler());
		r.put(4, new jane.handler.testserver.TestRpcBeanHandler());
		return r;
	}
}
