package com.contentstack.sdk;

public abstract class ContentTypesCallback implements ResultCallBack {

    public abstract void onCompletion(ContentTypesModel contentTypesModel, Error error);

    void onRequestFinish(ContentTypesModel contentTypesModel) {
        onCompletion(contentTypesModel, null);
    }

    @Override
    public void onRequestFail(ResponseType responseType, Error error) {
        onCompletion(null, error);
    }
}
