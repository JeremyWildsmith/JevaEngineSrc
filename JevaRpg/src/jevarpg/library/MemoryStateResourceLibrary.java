package jevarpg.library;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import jeva.StatelessEnvironmentException;
import jeva.UnresolvedResourcePathException;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;

public class MemoryStateResourceLibrary extends ResourceLibrary
{
    private HashMap<String, ByteArrayBuffer> m_states = new HashMap<String, ByteArrayBuffer>();

    @Override
    public OutputStream createState(String path) throws StatelessEnvironmentException
    {
        String resolvedPath = resolvePath(path);
        m_states.put(resolvedPath, new ByteArrayBuffer());

        return new MemoryStateOutputStream(m_states.get(resolvedPath));
    }

    @Override
    public InputStream openState(String path) throws StatelessEnvironmentException
    {
        String resolvedPath = resolvePath(path);

        if (!m_states.containsKey(resolvedPath))
            throw new UnresolvedResourcePathException(resolvedPath);

        return new ByteInputStream(m_states.get(resolvedPath).getRawData(), m_states.get(resolvedPath).size());
    }

    private class MemoryStateOutputStream extends OutputStream
    {
        private ByteArrayBuffer m_buffer;

        public MemoryStateOutputStream(ByteArrayBuffer buffer)
        {
            m_buffer = buffer;
        }

        @Override
        public void write(int b) throws IOException
        {
            m_buffer.write(b);
        }

    }
}
