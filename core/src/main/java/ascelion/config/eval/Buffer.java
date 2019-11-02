
package ascelion.config.eval;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

final class Buffer {

	char[] content;

	final int offset;
	int count;

	Buffer(String content) {
		this(content.toCharArray(), 0, content.length());
	}

	private Buffer(char[] content, int offset, int count) {
		this.content = content;
		this.offset = offset;
		this.count = count;
	}

	@Override
	public int hashCode() {
		int hash = 1;

		for (int ofs = this.offset; ofs < this.offset + this.count; ofs++) {
			hash = 31 * hash + this.content[ofs];
		}

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Buffer)) {
			return false;
		}

		final Buffer that = (Buffer) obj;

		if (this.count != that.count) {
			return false;
		}

		for (int ofs = 0; ofs < this.count; ofs++) {
			if (this.content[this.offset + ofs] != that.content[that.offset + ofs]) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return new String(this.content, this.offset, this.count);
	}

	int match(char[] txt, int ofs) {
		if (txt.length < 1) {
			throw new IllegalArgumentException("Refused to match empty pattern");
		}
		if (ofs < this.offset) {
			throw new IllegalArgumentException("Offset before the beginning of buffer");
		}

		for (int o = ofs, z = ofs + txt.length; o < z; o++) {
			if (o > this.offset + this.count) {
				return ofs + 1;
			}
			if (txt[o - ofs] != this.content[o]) {
				return ofs + 1;
			}
		}

		return ofs;
	}

	int find(char[] txt, int ofs) {
		while (ofs < this.offset + this.count) {
			final int next = match(txt, ofs);

			if (next == ofs) {
				return next;
			}

			ofs = next;
		}

		return -1;
	}

	int delete(int offset) {
		return delete(offset, 1);
	}

	int delete(int offset, int count) {
		this.count -= count;

		arraycopy(this.content, offset + count, this.content, offset, this.count - offset);

		return count;
	}

	int replace(int offset, int count, String text) {
		return -delete(offset, count) + insert(offset, text);
	}

	int insert(int offset, String text) {
		if (text != null) {
			final int z = text.length();

			if (z > 0) {
				final int newZ = this.count + z;

				if (newZ > this.content.length) {
					this.content = copyOf(this.content, newZ);
				}

				arraycopy(this.content, offset, this.content, offset + z, newZ - offset - z);
				arraycopy(text.toCharArray(), 0, this.content, offset, z);

				this.count = newZ;
			}

			return z;
		}

		return 0;
	}

	Buffer newBuffer(int offset, int count) {
		return new Buffer(copyOfRange(this.content, offset, offset + count), 0, count);
	}

	Buffer subBuffer(int offset, int count) {
		return new Buffer(this.content, offset, count);
	}

	String toString(int offset, int count) {
		return new String(this.content, offset, count);
	}
}
