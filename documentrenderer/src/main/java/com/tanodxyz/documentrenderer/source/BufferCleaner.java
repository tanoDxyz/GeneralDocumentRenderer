package com.tanodxyz.documentrenderer.source;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

class BufferCleaner {
    Class<?> unmappableBufferClass;
    final Method method;
    final Object theUnsafe;

    BufferCleaner(final Class<?> unmappableBufferClass, final Method method, final Object theUnsafe) {
        this.unmappableBufferClass = unmappableBufferClass;
        this.method = method;
        this.theUnsafe = theUnsafe;
    }

    void freeBuffer(String resourceDescription, final java.nio.ByteBuffer buffer) throws IOException {
        assert Objects.equals(void.class, method.getReturnType());
        assert method.getParameterTypes().length == 1;
        assert Objects.equals(ByteBuffer.class, method.getParameterTypes()[0]);
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("unmapping only works with direct buffers");
        }
        if (!unmappableBufferClass.isInstance(buffer)) {
            throw new IllegalArgumentException("buffer is not an instance of " + unmappableBufferClass.getName());
        }
        final Throwable error = AccessController.doPrivileged(new PrivilegedAction<Throwable>() {
            public Throwable run() {
                try {
                    method.invoke(theUnsafe, buffer);
                    return null;
                } catch (IllegalAccessException  e) {
                    return e;
                } catch (InvocationTargetException e) {
                    return e;
                }
            }
        });
        if (error != null) {
            throw new IOException("Unable to unmap the mapped buffer: " + resourceDescription, error);
        }
    }

    static Object unmapHackImpl() {
        try {
            // *** sun.misc.Unsafe unmapping (Java 9+) ***
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Method method = unsafeClass.getDeclaredMethod("invokeCleaner", ByteBuffer.class);
            final Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            final Object theUnsafe = f.get(null);
            return new BufferCleaner(ByteBuffer.class, method, theUnsafe);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}