using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace Jane
{
	/**
	 * 基于Octets的可扩展字节流的类型;
	 * 包括各种所需的序列化/反序列化;
	 */
	[Serializable]
	public class OctetsStream : Octets
	{
		protected int pos; // 当前的读写位置;

		public new static OctetsStream wrap(byte[] data, int size)
		{
			OctetsStream os = new OctetsStream();
			os.buffer = data;
			if(size > data.Length) os.count = data.Length;
			else if(size < 0)      os.count = 0;
			else                   os.count = size;
			return os;
		}

		public new static OctetsStream wrap(byte[] data)
		{
			OctetsStream os = new OctetsStream();
			os.buffer = data;
			os.count = data.Length;
			return os;
		}

		public static OctetsStream wrap(Octets o)
		{
			OctetsStream os = new OctetsStream();
			os.buffer = o.array();
			os.count = o.size();
			return os;
		}

		public OctetsStream()
		{
		}

		public OctetsStream(int size) : base(size)
		{
		}

		public OctetsStream(Octets o) : base(o)
		{
		}

		public OctetsStream(byte[] data) : base(data)
		{
		}

		public OctetsStream(byte[] data, int pos, int size) : base(data, pos, size)
		{
		}

		public bool eos()
		{
			return pos >= count;
		}

		public int position()
		{
			return pos;
		}

		public void setPosition(int pos)
		{
			this.pos = pos;
		}

		public override int remain()
		{
			return count - pos;
		}

		public OctetsStream wraps(Octets o)
		{
			buffer = o.array();
			count = o.size();
			return this;
		}

		public override object Clone()
		{
			OctetsStream os = new OctetsStream(this);
			os.pos = pos;
			return os;
		}

		public override string ToString()
		{
			return "[" + pos + '/' + count + '/' + buffer.Length + ']';
		}

		public override StringBuilder dump(StringBuilder s)
		{
			if(s == null) s = new StringBuilder(count * 3 + 16);
			return base.dump(s).Append(':').Append(pos);
		}

		public OctetsStream marshal1(byte x)
		{
			int count_new = count + 1;
			reserve(count_new);
			buffer[count] = x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal2(int x)
		{
			int count_new = count + 2;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 8);
			buffer[count + 1] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal3(int x)
		{
			int count_new = count + 3;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 16);
			buffer[count + 1] = (byte)(x >> 8);
			buffer[count + 2] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal4(int x)
		{
			int count_new = count + 4;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 24);
			buffer[count + 1] = (byte)(x >> 16);
			buffer[count + 2] = (byte)(x >> 8);
			buffer[count + 3] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal5(byte b, int x)
		{
			int count_new = count + 5;
			reserve(count_new);
			buffer[count    ] = b;
			buffer[count + 1] = (byte)(x >> 24);
			buffer[count + 2] = (byte)(x >> 16);
			buffer[count + 3] = (byte)(x >> 8);
			buffer[count + 4] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal5(long x)
		{
			int count_new = count + 5;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 32);
			buffer[count + 1] = (byte)(x >> 24);
			buffer[count + 2] = (byte)(x >> 16);
			buffer[count + 3] = (byte)(x >> 8);
			buffer[count + 4] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal6(long x)
		{
			int count_new = count + 6;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 40);
			buffer[count + 1] = (byte)(x >> 32);
			buffer[count + 2] = (byte)(x >> 24);
			buffer[count + 3] = (byte)(x >> 16);
			buffer[count + 4] = (byte)(x >> 8);
			buffer[count + 5] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal7(long x)
		{
			int count_new = count + 7;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 48);
			buffer[count + 1] = (byte)(x >> 40);
			buffer[count + 2] = (byte)(x >> 32);
			buffer[count + 3] = (byte)(x >> 24);
			buffer[count + 4] = (byte)(x >> 16);
			buffer[count + 5] = (byte)(x >> 8);
			buffer[count + 6] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal8(long x)
		{
			int count_new = count + 8;
			reserve(count_new);
			buffer[count    ] = (byte)(x >> 56);
			buffer[count + 1] = (byte)(x >> 48);
			buffer[count + 2] = (byte)(x >> 40);
			buffer[count + 3] = (byte)(x >> 32);
			buffer[count + 4] = (byte)(x >> 24);
			buffer[count + 5] = (byte)(x >> 16);
			buffer[count + 6] = (byte)(x >> 8);
			buffer[count + 7] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal9(byte b, long x)
		{
			int count_new = count + 9;
			reserve(count_new);
			buffer[count    ] = b;
			buffer[count + 1] = (byte)(x >> 56);
			buffer[count + 2] = (byte)(x >> 48);
			buffer[count + 3] = (byte)(x >> 40);
			buffer[count + 4] = (byte)(x >> 32);
			buffer[count + 5] = (byte)(x >> 24);
			buffer[count + 6] = (byte)(x >> 16);
			buffer[count + 7] = (byte)(x >> 8);
			buffer[count + 8] = (byte)x;
			count = count_new;
			return this;
		}

		public OctetsStream marshal(bool b)
		{
			int count_new = count + 1;
			reserve(count_new);
			buffer[count] = (byte)(b ? 1 : 0);
			count = count_new;
			return this;
		}

		public OctetsStream marshal(sbyte x)
		{
			return marshal((int)x);
		}

		public OctetsStream marshal(short x)
		{
			return marshal((int)x);
		}

		public OctetsStream marshal(char x)
		{
			return marshal((int)x);
		}

		public OctetsStream marshal(int x)
		{
			if(x >= 0)
			{
			    if(x < 0x40)      return marshal1((byte)x);        // 00xx xxxx
			    if(x < 0x2000)    return marshal2(x + 0x4000);     // 010x xxxx +1B
			    if(x < 0x100000)  return marshal3(x + 0x600000);   // 0110 xxxx +2B
			    if(x < 0x8000000) return marshal4(x + 0x70000000); // 0111 0xxx +3B
			                      return marshal5((byte)0x78, x);  // 0111 1000 +4B
			}
			if(x >= -0x40)        return marshal1((byte)x);        // 11xx xxxx
			if(x >= -0x2000)      return marshal2(x - 0x4000);     // 101x xxxx +1B
			if(x >= -0x100000)    return marshal3(x - 0x600000);   // 1001 xxxx +2B
			if(x >= -0x8000000)   return marshal4(x - 0x70000000); // 1000 1xxx +3B
			                      return marshal5((byte)0x87, x);  // 1000 0111 +4B
		}

		public static int marshalLen(int x)
		{
			if(x >= 0)
			{
			    if(x < 0x40)      return 1;
			    if(x < 0x2000)    return 2;
			    if(x < 0x100000)  return 3;
			    if(x < 0x8000000) return 4;
			                      return 5;
			}
			if(x >= -0x40)        return 1;
			if(x >= -0x2000)      return 2;
			if(x >= -0x100000)    return 3;
			if(x >= -0x8000000)   return 4;
			                      return 5;
		}

		public OctetsStream marshal(long x)
		{
			if(x >= 0)
			{
			    if(x < 0x8000000)         return marshal((int)x);
			    if(x < 0x400000000L)      return marshal5(x + 0x7800000000L);          // 0111 10xx +4B
			    if(x < 0x20000000000L)    return marshal6(x + 0x7c0000000000L);        // 0111 110x +5B
			    if(x < 0x1000000000000L)  return marshal7(x + 0x7e000000000000L);      // 0111 1110 +6B
			    if(x < 0x80000000000000L) return marshal8(x + 0x7f00000000000000L);    // 0111 1111 0+7B
			    return marshal9((byte)0x7f, x + unchecked((long)0x8000000000000000L)); // 0111 1111 1+8B
			}
			if(x >= -0x8000000)           return marshal((int)x);
			if(x >= -0x400000000L)        return marshal5(x - 0x7800000000L);          // 1000 01xx +4B
			if(x >= -0x20000000000L)      return marshal6(x - 0x7c0000000000L);        // 1000 001x +5B
			if(x >= -0x1000000000000L)    return marshal7(x - 0x7e000000000000L);      // 1000 0001 +6B
			if(x >= -0x80000000000000L)   return marshal8(x - 0x7f00000000000000L);    // 1000 0000 1+7B
			return marshal9((byte)0x80, x - unchecked((long)0x8000000000000000L));     // 1000 0000 0+8B
		}

		public static int marshalLen(long x)
		{
			if(x >= 0)
			{
			    if(x < 0x8000000)         return marshalLen((int)x);
			    if(x < 0x400000000L)      return 5;
			    if(x < 0x20000000000L)    return 6;
			    if(x < 0x1000000000000L)  return 7;
			    if(x < 0x80000000000000L) return 8;
			                              return 9;
			}
			if(x >= -0x8000000)           return marshalLen((int)x);
			if(x >= -0x400000000L)        return 5;
			if(x >= -0x20000000000L)      return 6;
			if(x >= -0x1000000000000L)    return 7;
			if(x >= -0x80000000000000L)   return 8;
			                              return 9;
		}

		public OctetsStream marshalUInt(int x)
		{
			if(x < 0x80)      return marshal1((byte)(x > 0 ? x : 0));          // 0xxx xxxx
			if(x < 0x4000)    return marshal2(x + 0x8000);                     // 10xx xxxx +1B
			if(x < 0x200000)  return marshal3(x + 0xc00000);                   // 110x xxxx +2B
			if(x < 0x1000000) return marshal4(x + unchecked((int)0xe0000000)); // 1110 xxxx +3B
			                  return marshal5((byte)0xf0, x);                  // 1111 0000 +4B
		}

		public int marshalUIntBack(int p, int x)
		{
			int t = count;
			if(p < 5 || p > t) throw new ArgumentException("p=" + p + ", _count=" + _count);
			if(x < 0x80)      { count = p - 1; marshal1((byte)(x > 0 ? x : 0));          count = t; return 1; }
			if(x < 0x4000)    { count = p - 2; marshal2(x + 0x8000);                     count = t; return 2; }
			if(x < 0x200000)  { count = p - 3; marshal3(x + 0xc00000);                   count = t; return 3; }
			if(x < 0x1000000) { count = p - 4; marshal4(x + unchecked((int)0xe0000000)); count = t; return 4; }
			                  { count = p - 5; marshal5((byte)0xf0, x);                  count = t; return 5; }
		}

		public static int marshalUIntLen(int x)
		{
			if(x < 0x80)      return 1;
			if(x < 0x4000)    return 2;
			if(x < 0x200000)  return 3;
			if(x < 0x1000000) return 4;
			                  return 5;
		}

		public OctetsStream marshalUTF8(char x)
		{
			if(x < 0x80)  return marshal1((byte)x);                                              // 0xxx xxxx
			if(x < 0x800) return marshal2(((x << 2) & 0x1f00) + (x & 0x3f) + 0xc080);            // 110x xxxx  10xx xxxx
			return marshal3(((x << 4) & 0xf0000) + ((x << 2) & 0x3f00) + (x & 0x3f) + 0xe08080); // 1110 xxxx  10xx xxxx  10xx xxxx
		}

		public OctetsStream marshal(float x)
		{
			return marshal4(BitConverter.ToInt32(BitConverter.GetBytes(x), 0));
		}

		public OctetsStream marshal(double x)
		{
			return marshal8(BitConverter.ToInt64(BitConverter.GetBytes(x), 0));
		}

		public OctetsStream marshal(byte[] bytes)
		{
			marshalUInt(bytes.Length);
			append(bytes, 0, bytes.Length);
			return this;
		}

		public OctetsStream marshal(Octets o)
		{
			if(o == null)
			{
				marshal1((byte)0);
				return this;
			}
			marshalUInt(o.size());
			append(o.array(), 0, o.size());
			return this;
		}

		public OctetsStream marshal(string str)
		{
			int cn;
			if(str == null || (cn = str.Length) <= 0)
			{
				marshal1((byte)0);
				return this;
			}
			int bn = 0;
			for(int i = 0; i < cn; ++i)
			{
				int c = str[i];
				if(c < 0x80) ++bn;
				else bn += (c < 0x800 ? 2 : 3);
			}
			marshalUInt(bn);
			reserve(count + bn);
			if(bn == cn)
			{
				for(int i = 0; i < cn; ++i)
					marshal1((byte)str[i]);
			}
			else
			{
				for(int i = 0; i < cn; ++i)
					marshalUTF8(str[i]);
			}
			return this;
		}

		public OctetsStream marshal<T>(T b) where T : IBean
		{
			return b != null ? b.Marshal(this) : marshal1((byte)0);
		}

		public OctetsStream marshal<T>(ref T b) where T : IBean
		{
			return b != null ? b.Marshal(this) : marshal1((byte)0);
		}

		public static int getKVType(object o)
		{
			if(o is IConvertible)
			{
				if(o is float) return 4;
				if(o is double) return 5;
				if(o is string) return 1;
				return 0;
			}
			if(o is IBean) return 2;
			return 1;
		}

		public OctetsStream marshalVar(int id, object o)
		{
			if(id < 1 || id > 62) throw new ArgumentException("id must be in [1,62]: " + id);
			if(o is IConvertible)
			{
				if(o is float)
				{
					float v = (float)o;
					if(v != 0) marshal2((id << 10) + 0x308).marshal(v);
				}
				else if(o is double)
				{
					double v = (double)o;
					if(v != 0) marshal2((id << 10) + 0x309).marshal(v);
				}
				else if(o is string)
				{
					string str = (string)o;
					if(str.Length > 0) marshal1((byte)((id << 2) + 1)).marshal(str);
				}
				else
				{
					long v = ((IConvertible)o).ToInt64(null);
					if(v != 0) marshal1((byte)(id << 2)).marshal(v);
				}
			}
			else if(o is IBean)
			{
				int n = count;
				((IBean)o).Marshal(marshal1((byte)((id << 2) + 2)));
				if(count - n < 3) resize(n);
			}
			else if(o is Octets)
			{
				Octets oct = (Octets)o;
				if(!oct.empty()) marshal1((byte)((id << 2) + 1)).marshal(oct);
			}
			else if(o is IDictionary)
			{
				IDictionary dic = (IDictionary)o;
				int n = dic.Count;
				if(n > 0)
				{
					IDictionaryEnumerator de = dic.GetEnumerator();
					de.MoveNext();
					int ktype = getKVType(de.Key);
					int vtype = getKVType(de.Value);
					marshal2((id << 10) + 0x340 + (ktype << 3) + vtype).marshalUInt(n);
					do
						marshalKV(ktype, de.Key).marshalKV(vtype, de.Value);
					while(de.MoveNext());
				}
			}
			else if(o is ICollection)
			{
				ICollection list = (ICollection)o;
				int n = list.Count;
				if(n > 0)
				{
					IEnumerator e = list.GetEnumerator();
					e.MoveNext();
					int vtype = getKVType(e.Current);
					marshal2((id << 10) + 0x300 + vtype).marshalUInt(n);
					do
						marshalKV(vtype, e.Current);
					while(e.MoveNext());
				}
			}
			return this;
		}

		public OctetsStream marshalKV(int kvtype, object o)
		{
			switch(kvtype)
			{
			case 0:
				if(o is int) marshal((int)o);
				else if(o is long) marshal((long)o);
				else if(o is sbyte) marshal((int)(sbyte)o);
				else if(o is short) marshal((int)(short)o);
				else if(o is char) marshal((int)(char)o);
				else if(o is bool) marshal1((byte)((bool)o ? 1 : 0));
				else if(o is float) marshal((long)(float)o);
				else if(o is double) marshal((long)(double)o);
				else marshal1((byte)0);
				break;
			case 1:
				if(o is Octets) marshal((Octets)o);
				else if(o != null) marshal(o.ToString());
				else marshal1((byte)0);
				break;
			case 2:
				if(o is IBean) marshal((IBean)o);
				else marshal1((byte)0);
				break;
			case 4:
				if(o is float) marshal((float)o);
				else if(o is double) marshal((float)(double)o);
				else if(o is int) marshal((float)(int)o);
				else if(o is long) marshal((float)(long)o);
				else if(o is sbyte) marshal((float)(sbyte)o);
				else if(o is short) marshal((float)(short)o);
				else if(o is char) marshal((float)(char)o);
				else if(o is bool) marshal((float)((bool)o ? 1 : 0));
				else marshal(0.0f);
				break;
			case 5:
				if(o is double) marshal((double)o);
				else if(o is float) marshal((double)(float)o);
				else if(o is int) marshal((double)(int)o);
				else if(o is long) marshal((double)(long)o);
				else if(o is sbyte) marshal((double)(sbyte)o);
				else if(o is short) marshal((double)(short)o);
				else if(o is char) marshal((double)(char)o);
				else if(o is bool) marshal((double)((bool)o ? 1 : 0));
				else marshal(0.0);
				break;
			default:
				throw new ArgumentException("kvtype must be in {0,1,2,4,5}: " + kvtype);
			}
			return this;
		}

		public sbyte unmarshalInt1()
		{
			if(pos >= count) throw new MarshalEOFException();
			return (sbyte)buffer[pos++];
		}

		public byte unmarshalUInt1()
		{
			if(pos >= count) throw new MarshalEOFException();
			return buffer[pos++];
		}

		public int unmarshalInt2()
		{
			int pos_new = pos + 2;
			if(pos_new > count) throw new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			pos = pos_new;
			return ((sbyte)b0 << 8) + b1;
		}

		public int unmarshalUInt2()
		{
			int pos_new = pos + 2;
			if(pos_new > count) throw new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			pos = pos_new;
			return (b0 << 8) + b1;
		}

		public int unmarshalInt3()
		{
			int pos_new = pos + 3;
			if(pos_new > count) throw new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			byte b2 = buffer[pos + 2];
			pos = pos_new;
			return (b0 << 16) +
			       (b1 <<  8) +
			        b2;
		}

		public int unmarshalInt4()
		{
			int pos_new = pos + 4;
			if(pos_new > count) new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			byte b2 = buffer[pos + 2];
			byte b3 = buffer[pos + 3];
			pos = pos_new;
			return (b0 << 24) +
			       (b1 << 16) +
			       (b2 <<  8) +
			        b3;
		}

		public long unmarshalLong5()
		{
			int pos_new = pos + 5;
			if(pos_new > count) new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			byte b2 = buffer[pos + 2];
			byte b3 = buffer[pos + 3];
			byte b4 = buffer[pos + 4];
			pos = pos_new;
			return ((long)b0 << 32) +
			       ((long)b1 << 24) +
			       (      b2 << 16) +
			       (      b3 <<  8) +
			              b4;
		}

		public long unmarshalLong6()
		{
			int pos_new = pos + 6;
			if(pos_new > count) new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			byte b2 = buffer[pos + 2];
			byte b3 = buffer[pos + 3];
			byte b4 = buffer[pos + 4];
			byte b5 = buffer[pos + 5];
			pos = pos_new;
			return ((long)b0 << 40) +
			       ((long)b1 << 32) +
			       ((long)b2 << 24) +
			       (      b3 << 16) +
			       (      b4 <<  8) +
			              b5;
		}

		public long unmarshalLong7()
		{
			int pos_new = pos + 7;
			if(pos_new > count) new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			byte b2 = buffer[pos + 2];
			byte b3 = buffer[pos + 3];
			byte b4 = buffer[pos + 4];
			byte b5 = buffer[pos + 5];
			byte b6 = buffer[pos + 6];
			pos = pos_new;
			return ((long)b0 << 48) +
			       ((long)b1 << 40) +
			       ((long)b2 << 32) +
			       ((long)b3 << 24) +
			       (      b4 << 16) +
			       (      b5 <<  8) +
			              b6;
		}

		public long unmarshalLong8()
		{
			int pos_new = pos + 8;
			if(pos_new > count) new MarshalEOFException();
			byte b0 = buffer[pos    ];
			byte b1 = buffer[pos + 1];
			byte b2 = buffer[pos + 2];
			byte b3 = buffer[pos + 3];
			byte b4 = buffer[pos + 4];
			byte b5 = buffer[pos + 5];
			byte b6 = buffer[pos + 6];
			byte b7 = buffer[pos + 7];
			pos = pos_new;
			return ((long)b0 << 56) +
			       ((long)b1 << 48) +
			       ((long)b2 << 40) +
			       ((long)b3 << 32) +
			       ((long)b4 << 24) +
			       (      b5 << 16) +
			       (      b6 <<  8) +
			              b7;
		}

		public float unmarshalFloat()
		{
			return BitConverter.ToSingle(BitConverter.GetBytes(unmarshalInt4()), 0);
		}

		public double unmarshalDouble()
		{
			return BitConverter.ToDouble(BitConverter.GetBytes(unmarshalLong8()), 0);
		}

		public OctetsStream unmarshalSkip(int n)
		{
			if(n < 0) throw new MarshalException();
			int pos_new = pos + n;
			if(pos_new > count) throw new MarshalEOFException();
			if(pos_new < pos) throw new MarshalException();
			pos = pos_new;
			return this;
		}

		public OctetsStream unmarshalSkipOctets()
		{
			return unmarshalSkip(unmarshalUInt());
		}

		public OctetsStream unmarshalSkipBean()
		{
			for(;;)
			{
				int tag = unmarshalUInt1();
				if(tag == 0) return this;
				unmarshalSkipVar(tag & 3);
			}
		}

		public OctetsStream unmarshalSkipVar(int type)
		{
			switch(type)
			{
				case 0: return unmarshalSkipInt(); // int/long: [1~9]
				case 1: return unmarshalSkipOctets(); // octets: n [n]
				case 2: return unmarshalSkipBean(); // bean: ... 00
				case 3: return unmarshalSkipVarSub(unmarshalUInt1()); // float/double/list/dictionary: ...
				default: throw new MarshalException();
			}
		}

		public object unmarshalVar(int type)
		{
			switch(type)
			{
				case 0: return unmarshalLong();
				case 1: return unmarshalOctets();
				case 2: { DynBean db = new DynBean(); db.Unmarshal(this); return db; }
				case 3: return unmarshalVarSub(unmarshalUInt1());
				default: throw new MarshalException();
			}
		}

		public OctetsStream unmarshalSkipVarSub(int subtype) // [tkkkvvv] [4]/[8]/<n>[kv*n]
		{
			if(subtype == 8) return unmarshalSkip(4); // float: [4]
			if(subtype == 9) return unmarshalSkip(8); // double: [8]
			if(subtype < 8) // list: <n>[v*n]
			{
				subtype &= 7;
				for(int n = unmarshalUInt(); n > 0; --n)
					unmarshalSkipKV(subtype);
			}
			else // dictionary: <n>[kv*n]
			{
				int keytype = (subtype >> 3) & 7;
				subtype &= 7;
				for(int n = unmarshalUInt(); n > 0; --n)
				{
					unmarshalSkipKV(keytype);
					unmarshalSkipKV(subtype);
				}
			}
			return this;
		}

		public object unmarshalVarSub(int subtype)
		{
			if(subtype == 8) return unmarshalFloat();
			if(subtype == 9) return unmarshalDouble();
			if(subtype < 8)
			{
				subtype &= 7;
				int n = unmarshalUInt();
				List<object> list = new List<object>(n < 0x10000 ? n : 0x10000);
				for(; n > 0; --n)
					list.Add(unmarshalKV(subtype));
				return list;
			}
			int keytype = (subtype >> 3) & 7;
			subtype &= 7;
			int m = unmarshalUInt();
			IDictionary<object, object> map = new Dictionary<object, object>(m < 0x10000 ? m : 0x10000);
			for(; m > 0; --m)
				map.Add(unmarshalKV(keytype), unmarshalKV(subtype));
			return map;
		}

		public OctetsStream unmarshalSkipKV(int kvtype)
		{
			switch(kvtype)
			{
				case 0: return unmarshalSkipInt(); // int/long: [1~9]
				case 1: return unmarshalSkipOctets(); // octets: n [n]
				case 2: return unmarshalSkipBean(); // bean: ... 00
				case 4: return unmarshalSkip(4); // float: [4]
				case 5: return unmarshalSkip(8); // double: [8]
				default: throw new MarshalException();
			}
		}

		public object unmarshalKV(int kvtype)
		{
			switch(kvtype)
			{
				case 0: return unmarshalLong();
				case 1: return unmarshalOctets();
				case 2: { DynBean db = new DynBean(); db.Unmarshal(this); return db; }
				case 4: return unmarshalFloat();
				case 5: return unmarshalDouble();
				default: throw new MarshalException();
			}
		}

		public OctetsStream unmarshalSkipInt()
		{
			int b = unmarshalUInt1();
			switch(b >> 3)
			{
			case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
			case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: break;
			case 0x08: case 0x09: case 0x0a: case 0x0b: case 0x14: case 0x15: case 0x16: case 0x17: unmarshalSkip(1); break;
			case 0x0c: case 0x0d: case 0x12: case 0x13: unmarshalSkip(2); break;
			case 0x0e: case 0x11: unmarshalSkip(3); break;
			case 0x0f:
				switch(b & 7)
				{
				case 0: case 1: case 2: case 3: unmarshalSkip(4); break;
				case 4: case 5:                 unmarshalSkip(5); break;
				case 6:                         unmarshalSkip(6); break;
				default: unmarshalSkip(6 + (unmarshalUInt1() >> 7)); break;
				}
				break;
			default: // 0x10
				switch(b & 7)
				{
				case 4: case 5: case 6: case 7: unmarshalSkip(4); break;
				case 2: case 3:                 unmarshalSkip(5); break;
				case 1:                         unmarshalSkip(6); break;
				default: unmarshalSkip(7 - (unmarshalUInt1() >> 7)); break;
				}
				break;
			}
			return this;
		}

		public int unmarshalInt()
		{
			int b = unmarshalInt1();
			switch((b >> 3) & 0x1f)
			{
			case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
			case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return b;
			case 0x08: case 0x09: case 0x0a: case 0x0b: return ((b - 0x40) <<  8) + unmarshalUInt1();
			case 0x14: case 0x15: case 0x16: case 0x17: return ((b + 0x40) <<  8) + unmarshalUInt1();
			case 0x0c: case 0x0d:                       return ((b - 0x60) << 16) + unmarshalUInt2();
			case 0x12: case 0x13:                       return ((b + 0x60) << 16) + unmarshalUInt2();
			case 0x0e:                                  return ((b - 0x70) << 24) + unmarshalInt3();
			case 0x11:                                  return ((b + 0x70) << 24) + unmarshalInt3();
			case 0x0f:
				switch(b & 7)
				{
				case 0: case 1: case 2: case 3: return unmarshalInt4();
				case 4: case 5:                 return unmarshalSkip(1).unmarshalInt4();
				case 6:                         return unmarshalSkip(2).unmarshalInt4();
				default: return unmarshalSkip(2 + (unmarshalUInt1() >> 7)).unmarshalInt4();
				}
			default: // 0x10
				switch(b & 7)
				{
				case 4: case 5: case 6: case 7: return unmarshalInt4();
				case 2: case 3:                 return unmarshalSkip(1).unmarshalInt4();
				case 1:                         return unmarshalSkip(2).unmarshalInt4();
				default: return unmarshalSkip(3 - (unmarshalUInt1() >> 7)).unmarshalInt4();
				}
			}
		}

		public long unmarshalLong()
		{
			int b = unmarshalInt1();
			switch((b >> 3) & 0x1f)
			{
			case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
			case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return b;
			case 0x08: case 0x09: case 0x0a: case 0x0b: return ((b - 0x40) <<  8) + unmarshalUInt1();
			case 0x14: case 0x15: case 0x16: case 0x17: return ((b + 0x40) <<  8) + unmarshalUInt1();
			case 0x0c: case 0x0d:                       return ((b - 0x60) << 16) + unmarshalUInt2();
			case 0x12: case 0x13:                       return ((b + 0x60) << 16) + unmarshalUInt2();
			case 0x0e:                                  return ((b - 0x70) << 24) + unmarshalInt3();
			case 0x11:                                  return ((b + 0x70) << 24) + unmarshalInt3();
			case 0x0f:
				switch(b & 7)
				{
				case 0: case 1: case 2: case 3: return ((long)(b - 0x78) << 32) + (unmarshalInt4() & 0xffffffffL);
				case 4: case 5:                 return ((long)(b - 0x7c) << 40) + unmarshalLong5();
				case 6:                         return unmarshalLong6();
				default: long r = unmarshalLong7(); return r < 0x80000000000000L ?
						r : ((r - 0x80000000000000L) << 8) + unmarshalUInt1();
				}
			default: // 0x10
				switch(b & 7)
				{
				case 4: case 5: case 6: case 7: return ((long)(b + 0x78) << 32) + (unmarshalInt4() & 0xffffffffL);
				case 2: case 3:                 return ((long)(b + 0x7c) << 40) + unmarshalLong5();
				case 1:                         return unchecked((long)0xffff000000000000L) + unmarshalLong6();
				default: long r = unmarshalLong7(); return r >= 0x80000000000000L ?
						unchecked((long)0xff00000000000000L) + r : ((r + 0x80000000000000L) << 8) + unmarshalUInt1();
				}
			}
		}

		public int unmarshalUInt()
		{
			int b = unmarshalUInt1();
			switch(b >> 4)
			{
			case  0: case  1: case  2: case  3: case 4: case 5: case 6: case 7: return b;
			case  8: case  9: case 10: case 11: return ((b & 0x3f) <<  8) + unmarshalUInt1();
			case 12: case 13:                   return ((b & 0x1f) << 16) + unmarshalUInt2();
			case 14:                            return ((b & 0x0f) << 24) + unmarshalInt3();
			default: int r = unmarshalInt4(); if(r < 0) throw new MarshalException(); return r;
			}
		}

		public char unmarshalUTF8()
		{
			int b = unmarshalUInt1();
			if(b < 0x80) return (char)b;
			if(b < 0xe0) return (char)(((b & 0x1f) << 6) + (unmarshalUInt1() & 0x3f));
			int c = unmarshalUInt1();
			return (char)(((b & 0xf) << 12) + ((c & 0x3f) << 6) + (unmarshalUInt1() & 0x3f));
		}

		public int unmarshalInt(int type)
		{
			if(type == 0) return unmarshalInt();
			if(type == 3)
			{
				type = unmarshalUInt1();
				if(type == 8) return (int)unmarshalFloat();
				if(type == 9) return (int)unmarshalDouble();
				unmarshalSkipVarSub(type);
				return 0;
			}
			unmarshalSkipVar(type);
			return 0;
		}

		public long unmarshalLong(int type)
		{
			if(type == 0) return unmarshalLong();
			if(type == 3)
			{
				type = unmarshalUInt1();
				if(type == 8) return (long)unmarshalFloat();
				if(type == 9) return (long)unmarshalDouble();
				unmarshalSkipVarSub(type);
				return 0;
			}
			unmarshalSkipVar(type);
			return 0;
		}

		public float unmarshalFloat(int type)
		{
			if(type == 3)
			{
				type = unmarshalUInt1();
				if(type == 8) return unmarshalFloat();
				if(type == 9) return (float)unmarshalDouble();
				unmarshalSkipVarSub(type);
				return 0;
			}
			if(type == 0) return unmarshalLong();
			unmarshalSkipVar(type);
			return 0;
		}

		public double unmarshalDouble(int type)
		{
			if(type == 3)
			{
				type = unmarshalUInt1();
				if(type == 9) return unmarshalDouble();
				if(type == 8) return unmarshalFloat();
				unmarshalSkipVarSub(type);
				return 0;
			}
			if(type == 0) return unmarshalLong();
			unmarshalSkipVar(type);
			return 0;
		}

		public int unmarshalIntKV(int type)
		{
			if(type == 0) return unmarshalInt();
			if(type == 4) return (int)unmarshalFloat();
			if(type == 5) return (int)unmarshalDouble();
			unmarshalSkipKV(type);
			return 0;
		}

		public long unmarshalLongKV(int type)
		{
			if(type == 0) return unmarshalLong();
			if(type == 4) return (long)unmarshalFloat();
			if(type == 5) return (long)unmarshalDouble();
			unmarshalSkipKV(type);
			return 0;
		}

		public float unmarshalFloatKV(int type)
		{
			if(type == 4) return unmarshalFloat();
			if(type == 5) return (float)unmarshalDouble();
			if(type == 0) return unmarshalLong();
			unmarshalSkipKV(type);
			return 0;
		}

		public double unmarshalDoubleKV(int type)
		{
			if(type == 5) return unmarshalDouble();
			if(type == 4) return unmarshalFloat();
			if(type == 0) return unmarshalLong();
			unmarshalSkipKV(type);
			return 0;
		}

		public byte[] unmarshalBytes()
		{
			int size = unmarshalUInt();
			if(size <= 0) return EMPTY;
			int pos_new = pos + size;
			if(pos_new > count) new MarshalEOFException();
			if(pos_new < pos) new MarshalException();
			byte[] r = new byte[size];
			Buffer.BlockCopy(buffer, pos, r, 0, size);
			pos = pos_new;
			return r;
		}

		public Octets unmarshalOctets()
		{
			return Octets.wrap(unmarshalBytes());
		}

		public Octets unmarshalOctetsKV(int type)
		{
			if(type == 1) return unmarshalOctets();
			unmarshalSkipKV(type);
			return new Octets();
		}

		public OctetsStream unmarshal(Octets o)
		{
			int size = unmarshalUInt();
			if(size <= 0)
			{
				o.clear();
				return this;
			}
			int pos_new = pos + size;
			if(pos_new > count) new MarshalEOFException();
			if(pos_new < pos) new MarshalException();
			o.replace(buffer, pos, size);
			pos = pos_new;
			return this;
		}

		public OctetsStream unmarshal(Octets o, int type)
		{
			if(type == 1) return unmarshal(o);
			unmarshalSkipVar(type);
			return this;
		}

		public Octets unmarshalRaw(int size)
		{
			if(size <= 0) return new Octets();
			int pos_new = pos + size;
			if(pos_new > count) new MarshalEOFException();
			if(pos_new < pos) new MarshalException();
			Octets o = new Octets(buffer, pos, size);
			pos = pos_new;
			return o;
		}

		public OctetsStream unmarshal<T>(ref T b) where T : IBean
		{
			return b.Unmarshal(this);
		}

		public OctetsStream unmarshalBean<T>(ref T b, int type) where T : IBean
		{
			if(type == 2) return b.Unmarshal(this);
			unmarshalSkipVar(type);
			return this;
		}

		public T unmarshalBean<T>(T b) where T : IBean
		{
			b.Unmarshal(this);
			return b;
		}

		public T unmarshalBeanKV<T>(T b, int type) where T : IBean
		{
			if(type == 2)
				b.Unmarshal(this);
			else
				unmarshalSkipKV(type);
			return b;
		}

		public byte[] unmarshalBytes(int type)
		{
			if(type == 1) return unmarshalBytes();
			unmarshalSkipVar(type);
			return EMPTY;
		}

		public byte[] unmarshalBytesKV(int type)
		{
			if(type == 1) return unmarshalBytes();
			unmarshalSkipKV(type);
			return EMPTY;
		}

		public string unmarshalString()
		{
			int size = unmarshalUInt();
			if(size <= 0) return string.Empty;
			int pos_new = pos + size;
			if(pos_new > count) new MarshalEOFException();
			if(pos_new < pos) new MarshalException();
			char[] tmp = new char[size];
			int n = 0;
			while(pos < pos_new)
				tmp[n++] = unmarshalUTF8();
			pos = pos_new;
			return new string(tmp, 0, n);
		}

		public string unmarshalString(int type)
		{
			if(type == 1) return unmarshalString();
			if(type == 0) return unmarshalLong().ToString();
			if(type == 3)
			{
				type = unmarshalUInt1();
				if(type == 8) return unmarshalFloat().ToString();
				if(type == 9) return unmarshalDouble().ToString();
				unmarshalSkipVarSub(type);
			}
			else
				unmarshalSkipVar(type);
			return string.Empty;
		}

		public string unmarshalStringKV(int type)
		{
			if(type == 1) return unmarshalString();
			if(type == 0) return unmarshalLong().ToString();
			if(type == 4) return unmarshalFloat().ToString();
			if(type == 5) return unmarshalDouble().ToString();
			unmarshalSkipKV(type);
			return string.Empty;
		}
	}
}
