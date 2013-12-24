// This file is generated by genbeans tool. Do NOT edit it! @formatter:off
package sas.bean;

import sas.core.Bean;
import sas.core.MarshalException;
import sas.core.OctetsStream;
import sas.core.Util;

/**
 * 作为key或配置的bean
 */
public final class TestKeyBean extends Bean<TestKeyBean> implements Comparable<TestKeyBean>
{
	private static final long serialVersionUID = 0xbeac0364a4241981L;
	public  static final int BEAN_TYPE = 2;

	private /* 1*/ int key1; // KEY-1
	private /* 2*/ String key2; // KEY-2

	public TestKeyBean()
	{
		key2 = "";
	}

	public TestKeyBean(int key1, String key2)
	{
		this.key1 = key1;
		this.key2 = (key2 != null ? key2 : "");
	}

	@Override
	public void reset()
	{
		throw new UnsupportedOperationException();
	}

	public int getKey1()
	{
		return key1;
	}

	public String getKey2()
	{
		return key2;
	}

	@Override
	public int type()
	{
		return 2;
	}

	@Override
	public int initSize()
	{
		return 16;
	}

	@Override
	public int maxSize()
	{
		return 16;
	}

	@Override
	public TestKeyBean create()
	{
		return new TestKeyBean();
	}

	@Override
	public OctetsStream marshal(OctetsStream s)
	{
		if(this.key1 != 0) s.marshal1((byte)0x04).marshal(this.key1);
		if(!this.key2.isEmpty()) s.marshal1((byte)0x09).marshal(this.key2);
		return s.marshal1((byte)0);
	}

	@Override
	public OctetsStream unmarshal(OctetsStream s) throws MarshalException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TestKeyBean clone()
	{
		return new TestKeyBean(key1, key2);
	}

	@Override
	public int hashCode()
	{
		int h = 2 * 0x9e3779b1;
		h = h * 31 + 1 + this.key1;
		h = h * 31 + 1 + this.key2.hashCode();
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this) return true;
		if(!(o instanceof TestKeyBean)) return false;
		TestKeyBean b = (TestKeyBean)o;
		if(this.key1 != b.key1) return false;
		if(!this.key2.equals(b.key2)) return false;
		return getClass() == o.getClass();
	}

	@Override
	public int compareTo(TestKeyBean b)
	{
		if(b == this) return 0;
		if(b == null) return 1;
		int c;
		c = this.key1 - b.key1; if(c != 0) return c;
		c = this.key2.compareTo(b.key2); if(c != 0) return c;
		return 0;
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder(16 + 16 * 2).append('{');
		s.append(this.key1).append(',');
		s.append(this.key2).append(',');
		s.setLength(s.length() - 1);
		return s.append('}').toString();
	}

	@Override
	public StringBuilder toJson(StringBuilder s)
	{
		if(s == null) s = new StringBuilder(1024);
		s.append('{');
		s.append("\"key1\":").append(this.key1).append(',');
		Util.toJStr(s.append("\"key2\":"), this.key2).append(',');
		s.setLength(s.length() - 1);
		return s.append('}');
	}

	@Override
	public StringBuilder toLua(StringBuilder s)
	{
		if(s == null) s = new StringBuilder(1024);
		s.append('{');
		s.append("key1=").append(this.key1).append(',');
		Util.toJStr(s.append("key2="), this.key2).append(',');
		s.setLength(s.length() - 1);
		return s.append('}');
	}
}
