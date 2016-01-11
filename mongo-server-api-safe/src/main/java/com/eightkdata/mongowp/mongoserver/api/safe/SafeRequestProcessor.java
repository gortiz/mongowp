package com.eightkdata.mongowp.mongoserver.api.safe;

import com.eightkdata.mongowp.messages.request.*;
import com.eightkdata.mongowp.messages.response.ReplyMessage;
import com.eightkdata.mongowp.mongoserver.api.safe.impl.UpdateOpResult;
import com.eightkdata.mongowp.mongoserver.api.safe.pojos.QueryRequest;
import com.eightkdata.mongowp.mongoserver.callback.WriteOpResult;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.CommandNotSupportedException;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.MongoException;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nonnull;

/**
 *
 */
public interface SafeRequestProcessor extends CommandsExecutor{

    public void onConnectionActive(Connection connection);

    public void onConnectionInactive(Connection connection);

    @Nonnull
    public ReplyMessage getMore(Request request, GetMoreMessage getMoreMessage)
            throws MongoException;

    public ListenableFuture<?> killCursors(Request request, KillCursorsMessage killCursorsMessage)
            throws MongoException;

    public CommandsLibrary getCommandsLibrary();

    @Override
    public <Arg, Result> CommandReply<Result> execute(
            Command<? super Arg, ? super Result> command,
            CommandRequest<Arg> request) throws MongoException, CommandNotSupportedException;

    @Nonnull
    public ReplyMessage query(Request request, QueryRequest queryMessage)
            throws MongoException;

    public ListenableFuture<? extends WriteOpResult> insert(Request request, InsertMessage insertMessage)
            throws MongoException;

    public ListenableFuture<? extends UpdateOpResult> update(Request request, UpdateMessage deleteMessage)
            throws MongoException;

    public ListenableFuture<? extends WriteOpResult> delete(Request request, DeleteMessage deleteMessage)
            throws MongoException;

}