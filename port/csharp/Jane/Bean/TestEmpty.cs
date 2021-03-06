// This file is generated by genbeans tool. Do NOT edit it!
using System;
using System.Text;
using System.Collections.Generic;

namespace Jane.Bean
{
	/**
	 * 测试空bean;
	 */
	[Serializable]
	public class TestEmpty : IBean, IEquatable<TestEmpty>, IComparable<TestEmpty>
	{
		public const int BEAN_TYPE = 3;
		public int Serial { get; set; }

		public void Reset()
		{
		}

		public void Assign(TestEmpty b)
		{
		}
/**/
		public int Type()
		{
			return BEAN_TYPE;
		}

		public int InitSize()
		{
			return 0;
		}

		public int MaxSize()
		{
			return 0;
		}

		public void Init()
		{
		}

		public static TestEmpty Create()
		{
			TestEmpty b = new TestEmpty();
			b.Init();
			return b;
		}

		public static IBean CreateIBean()
		{
			IBean b = new TestEmpty();
			b.Init();
			return b;
		}

		public OctetsStream Marshal(OctetsStream s)
		{
			return s.Marshal1((byte)0);
		}

		public OctetsStream Unmarshal(OctetsStream s)
		{
			Init();
			for(;;) { int i = s.UnmarshalUInt1(), t = i & 3; if((i >>= 2) == 63) i += s.UnmarshalUInt1(); switch(i)
			{
				case 0: return s;
				default: s.UnmarshalSkipVar(t); break;
			}}
		}

		public object Clone()
		{
			return new TestEmpty();
		}

		public override int GetHashCode()
		{
			int h = unchecked(3 * (int)0x9e3779b1);
			return h;
		}

		public bool Equals(TestEmpty b)
		{
			return true;
		}

		public override bool Equals(object o)
		{
			if(!(o is TestEmpty)) return false;
			return true;
		}

		public static bool operator==(TestEmpty a, TestEmpty b)
		{
			return a.Equals(b);
		}

		public static bool operator!=(TestEmpty a, TestEmpty b)
		{
			return !a.Equals(b);
		}

		public int CompareTo(TestEmpty b)
		{
			return 0;
		}

		public int CompareTo(IBean b)
		{
			return b is TestEmpty ? CompareTo((TestEmpty)b) : 1;
		}

		public int CompareTo(object b)
		{
			return b is IBean ? CompareTo((IBean)b) : 1;
		}

		public override string ToString()
		{
			StringBuilder s = new StringBuilder(16 + 0 * 2).Append('{');
			return s.Append('}').ToString();
		}
#if TO_JSON_LUA
		public StringBuilder ToJson(StringBuilder s)
		{
			if(s == null) s = new StringBuilder(1024);
			s.Append('{');
			return s.Append('}');
		}

		public StringBuilder ToJson()
		{
			return ToJson(null);
		}

		public StringBuilder ToLua(StringBuilder s)
		{
			if(s == null) s = new StringBuilder(1024);
			s.Append('{');
			return s.Append('}');
		}

		public StringBuilder ToLua()
		{
			return ToLua(null);
		}
#endif
	}
}
