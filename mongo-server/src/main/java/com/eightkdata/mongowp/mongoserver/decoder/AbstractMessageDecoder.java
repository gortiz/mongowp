
package com.eightkdata.mongowp.mongoserver.decoder;

import com.eightkdata.mongowp.messages.request.RequestMessage;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.InvalidNamespaceException;

/**
 *
 */
public abstract class AbstractMessageDecoder<T extends RequestMessage> implements MessageDecoder<T>{

    protected String getDatabase(String namespace) throws InvalidNamespaceException {
        int firstDotIndex = getAndCheckFirstDot(namespace);
        if (firstDotIndex == namespace.length()) { //if there is no dot
            return namespace; //then all namespace is the database
        }
        return namespace.substring(0, getAndCheckFirstDot(namespace));
    }

    protected String getCollection(String namespace) throws InvalidNamespaceException {
        int firstDotIndex = getAndCheckFirstDot(namespace);
        if (firstDotIndex == namespace.length()) { //if there is no dot
            return null; //then there is no collection
        }
        return namespace.substring(firstDotIndex + 1);
    }

    private int getAndCheckFirstDot(String namespace) throws InvalidNamespaceException {
        int firstDot = namespace.indexOf('.');
        if (firstDot == 0) {
            throw new InvalidNamespaceException(namespace, "The first character shall not be a dot");
        }
        if (firstDot < 0) {
            return namespace.length();
        }
        if (firstDot == namespace.length() - 1) {
            throw new InvalidNamespaceException(namespace, "The last character shall not be the first dot");
        }
        return firstDot;
    }
}
