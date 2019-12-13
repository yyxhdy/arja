package jdkcompiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class JavaFileObjectImpl extends SimpleJavaFileObject {
	// If kind == CLASS, this stores byte code from openOutputStream
	private ByteArrayOutputStream byteCode;

	private final CharSequence source;

	JavaFileObjectImpl(final String name, final CharSequence source) {
		super(URI.create("memo:/" + name), Kind.SOURCE);
		this.source = source;
	}

	JavaFileObjectImpl(final String name, final Kind kind) {
		super(URI.create("memo:/" + name.replace('.', '/') + kind.extension), kind);
		source = null;
	}

	/**
	 * 
	 * 
	 * @see javax.tools.SimpleJavaFileObject#getCharContent(boolean)
	 */
	@Override
	public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws UnsupportedOperationException {
		if (source == null)
			throw new UnsupportedOperationException("getCharContent()");
		return source;
	}

	/**
	 * Return an input stream for reading the byte code
	 * 
	 * @see javax.tools.SimpleJavaFileObject#openInputStream()
	 */
	@Override
	public InputStream openInputStream() {
		return new ByteArrayInputStream(getByteCode());
	}

	/**
	 * Return an output stream for writing the bytecode
	 * 
	 * @see javax.tools.SimpleJavaFileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() {
		byteCode = new ByteArrayOutputStream();
		return byteCode;
	}

	/**
	 * @return the byte code generated by the compiler
	 */
	public byte[] getByteCode() {
		return byteCode.toByteArray();
	}

}