package com.contentstack.sdk;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.contentstack.sdk.Constants.*;

/**
 * A stack is a repository or a container that holds all the content/assets of
 * your site. It allows multiple users to create, edit, approve, and publish
 * their content within a single space.
 */
public class Stack {

    private final Logger logger = Logger.getLogger(Stack.class.getSimpleName());
    protected LinkedHashMap<String, Object> headers;
    protected Config config;
    protected String contentType;
    protected String apiKey;
    protected JSONObject syncParams = null;

    protected Stack() throws IllegalAccessException {
        throw new IllegalAccessException("Can Not Access Private Modifier");
    }

    protected Stack(@NotNull String apiKey) {
        this.apiKey = apiKey;
        this.headers = new LinkedHashMap<>();
    }

    protected void setConfig(Config config) {
        this.config = config;
        String urlDomain = config.host;

        if (!config.region.name().isEmpty()) {
            String region = config.region.name().toLowerCase();
            if (region.equalsIgnoreCase("eu")) {
                if (urlDomain.equalsIgnoreCase("cdn.contentstack.io")) {
                    urlDomain = "cdn.contentstack.com";
                }
                config.host = region + "-" + urlDomain;
            } else if (region.equalsIgnoreCase("azure_na")) {
                if (urlDomain.equalsIgnoreCase("cdn.contentstack.io")) {
                    urlDomain = "cdn.contentstack.com";
                }
                config.host = "azure-na" + "-" + urlDomain;
            }
        }

        includeLivePreview();
        // Set the endpoint
        String endpoint = config.scheme + config.host;
        this.config.setEndpoint(endpoint);
    }

    private void includeLivePreview() {
        try {
            if (config.enableLivePreview) {
                if (config.managementToken == null || config.managementToken.isEmpty()) {
                    throw new IllegalAccessException("managementToken is required");
                }
                if (config.livePreviewHost == null || config.livePreviewHost.isEmpty()) {
                    throw new IllegalAccessException("host is required");
                }
                config.host = config.livePreviewHost;
            }
        } catch (Exception e) {
            String info = "To enable live preview, managementToken and host are required";
            logger.warning(info);
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
    }

    /**
     * Live Preview lets content managers preview entry content across multiple
     * channels before saving or publishing it to a live website. You can edit an
     * entry and preview the content changes side by side in real-time.
     * <p>
     * <b>Note:</b> To be able to preview entry content, developers need to first
     * configure Live Preview for the frontend website and then enable it from the
     * stack settings section in Contentstack. You can set up the base URL and
     * environment across which you want to preview content.
     * <p>
     *
     * @param query the query of type {@link HashMap}
     * @return stack
     *         <p>
     *         <b>Example</b>
     *
     *
     *         stack = contentstack.Stack("apiKey", "deliveryToken", "environment");
     *         <p>
     *         HashMap queryMap = new HashMap();
     *         <p>
     *         stack.livePreviewQuery(queryMap)
     *
     */
    public Stack livePreviewQuery(Map<String, String> query) {
        if (this.config.enableLivePreview) {
            config.livePreviewHash = query.get("live_preview");
            config.livePreviewContentType = query.get(CONTENT_TYPE_UID);
        }
        return this;
    }

    /**
     * Content type defines the structure or schema of a page or a section of your
     * web or mobile property. To create content for your application, you are
     * required to first create a content type, and then create entries using the
     * content type.
     *
     * @param contentTypeUid Enter the unique ID of the content type of which you
     *                       want to retrieve the entries. The UID is often based on
     *                       the title of the content type and it is unique across a
     *                       stack.
     * @return the {@link ContentType}
     *         <p>
     *         <b>Example</b>
     *
     *         <pre>
     *  Stack stack = contentstack.Stack("apiKey",
     * "deliveryToken", "environment"); ContentType contentType = stack.contentType("contentTypeUid")
     *         </pre>
     */
    public ContentType contentType(String contentTypeUid) {
        this.contentType = contentTypeUid; // set contentTypeUid for stack instance
        ContentType ct = new ContentType(contentTypeUid);
        ct.setStackInstance(this);
        return ct;
    }

    /**
     * Assets refer to all the media files (images, videos, PDFs, audio files, and
     * so on) uploaded in your Contentstack repository for future use. These files
     * can be attached and used in multiple entries.
     * <P>
     * The Get a single asset request fetches the latest version of a specific asset
     * of a particular stack.
     * <p>
     *
     * @param uid uid of {@link Asset}
     * @return {@link Asset} instance <b>Tip:</b> If no version is mentioned, the
     *         request will retrieve the latest published version of the asset. To
     *         retrieve a specific version, use the version parameter, keep the
     *         environment parameter blank, and use the management token instead of
     *         the delivery token.
     *         <p>
     *         <b>Example</b> Stack stack = contentstack.Stack("apiKey",
     *         "deliveryToken", "environment"); Asset asset =
     *         stack.asset("assetUid");
     *
     */
    public Asset asset(@NotNull String uid) {
        Asset asset = new Asset(uid);
        asset.setStackInstance(this);
        return asset;
    }

    protected Asset asset() {
        Asset asset = new Asset();
        asset.setStackInstance(this);
        return asset;
    }

    /**
     * The Get all assets request fetches the list of all the assets of a particular
     * stack. It returns the content of each asset in JSON format.
     *
     * @return {@link AssetLibrary} asset library
     *         <p>
     *         <b>Example</b>
     *
     *         <pre>
     *         Stack stack = contentstack.Stack("apiKey", "deliveryToken", "environment");
     *         AssetLibrary assets = stack.assetLibrary();
     *         </pre>
     */
    public AssetLibrary assetLibrary() {
        AssetLibrary library = new AssetLibrary();
        library.setStackInstance(this);
        return library;
    }

    /**
     * Returns apiKey of particular stack
     *
     * @return {@link Stack} apiKey
     */
    public String getApplicationKey() {
        return apiKey;
    }

    /**
     * @return {@link Stack} accessToken
     * @deprecated This method is no longer acceptable to get access token.
     * <p> Use getDeliveryToken instead.
     */
    @Deprecated
    public String getAccessToken() {
        return (String) headers.get("access_token");
    }

    /**
     * Returns deliveryToken of particular stack
     *
     * @return deliveryToken delivery token
     */
    public String getDeliveryToken() {
        return (String) headers.get("access_token");
    }

    /**
     * Removes Header by key
     *
     * @param headerKey of the header
     *                  <p>
     *                  <b>Example:</b> stack.removeHeader("delivery_token");
     */
    public void removeHeader(String headerKey) {
        headers.remove(headerKey);
    }

    /**
     * Adds header to the stack
     *
     * @param headerKey   the header key
     * @param headerValue the header value
     */
    public void setHeader(@NotNull String headerKey, @NotNull String headerValue) {
        if (!headerKey.isEmpty() && !headerValue.isEmpty()) {
            headers.put(headerKey, headerValue);
        }
    }

    /**
     * Image transform string. This document is a detailed reference to Contentstack
     * Image Delivery API and covers the parameters that you can add to the URL to
     * retrieve, manipulate (or convert) image files and display it to your web or
     * mobile properties.
     *
     * @param imageUrl   the image url
     * @param parameters the parameters {@link LinkedHashMap}
     * @return the string
     */
    public String imageTransform(@NotNull String imageUrl, @NotNull Map<String, Object> parameters) {
        if (parameters.size() == 0) {
            return imageUrl;
        }
        String query = getQueryParam(parameters);
        if (imageUrl.contains("?")) {
            imageUrl += "&" + query;
        } else {
            imageUrl += "?" + query;
        }
        return imageUrl;
    }

    protected String getQueryParam(Map<String, Object> params) {
        return params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }

    /**
     * The Get all content types call returns comprehensive information of all the
     * content types available in a particular stack in your account..
     *
     * @param params   query parameters
     * @param callback ContentTypesCallback This call returns comprehensive
     *                 information of all the content types available in a
     *                 particular stack in your account.
     */
    public void getContentTypes(@NotNull JSONObject params, final ContentTypesCallback callback) {
        Iterator<String> keys = params.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = params.opt(key);
            params.put(key, value);
        }
        if (this.headers.containsKey(ENVIRONMENT)) {
            params.put(ENVIRONMENT, this.headers.get(ENVIRONMENT));
            params.put("include_count", true);
        }
        fetchContentTypes("content_types", params, this.headers, callback);
    }

    /**
     * The Sync request performs a complete sync of your app data. It returns all
     * the published entries and assets of the specified stack in response. The
     * response also contains a sync token, which you need to store, since this
     * token is used to get subsequent delta
     *
     * @param syncCallBack returns callback for sync result.
     *
     */
    public void sync(SyncResultCallBack syncCallBack) {
        syncParams = new JSONObject();
        syncParams.put("init", true);
        this.requestSync(syncCallBack);
    }

    /**
     * Sync pagination token.
     *
     * @param paginationToken If the response is paginated, use the pagination token
     *                        under this parameter.
     * @param syncCallBack    returns callback for sync result
     *                        <p>
     *                        If the result of the initial sync (or subsequent sync)
     *                        contains more than 100 records, the response would be
     *                        paginated. It provides pagination token in the
     *                        response. However, you do not have to use the
     *                        pagination token manually to get the next batch, the
     *                        SDK does that automatically until the sync is
     *                        complete. Pagination token can be used in case you
     *                        want to fetch only selected batches. It is especially
     *                        useful if the sync process is interrupted midway (due
     *                        to network issues, etc.). In such cases, this token
     *                        can be used to restart the sync process from where it
     *                        was interrupted. <br>
     *                        <br>
     *                        <b>Example :</b><br>
     *
     */
    public void syncPaginationToken(@NotNull String paginationToken, SyncResultCallBack syncCallBack) {
        this.sync(null);
        syncParams.put("pagination_token", paginationToken);
        this.requestSync(syncCallBack);
    }

    /**
     * Sync token.
     *
     * @param syncToken    Use the sync token that you received in the
     *                     previous/initial sync under this parameter.
     * @param syncCallBack returns callback for sync result
     *                     <p>
     *                     You can use the sync token (that you receive after
     *                     initial sync) to get the updated content next time. The
     *                     sync token fetches only the content that was added after
     *                     your last sync, and the details of the content that was
     *                     deleted or updated. <br>
     *                     <br>
     *                     <b>Example :</b><br>
     *
     *                     <pre class="prettyprint">
     *                     stack.syncToken(sync_token, new SyncResultCallBack()                                                                                                                                                                                                               ){ }
     *                     </pre>
     */
    public void syncToken(String syncToken, SyncResultCallBack syncCallBack) {
        this.sync(null);
        syncParams.put("sync_token", syncToken);
        this.requestSync(syncCallBack);
    }

    /**
     * Sync from date.
     *
     * @param fromDate     Enter the start date for initial sync.
     * @param syncCallBack Returns callback for sync result.
     *                     <p>
     *                     You can also initialize sync with entries published after
     *                     a specific date. To do this, use syncWithDate and specify
     *                     the start date as its value. <br>
     *                     <br>
     *                     <b>Example :</b><br>
     *
     *                     <pre class="prettyprint">
     *                     final Date start_date = sdf.parse("2018-10-07"); <P>
     *                     stack.syncFromDate(start_date, SyncResultCallBack()) {
     *
     *                     }
     *                     </pre>
     */
    public void syncFromDate(@NotNull Date fromDate, SyncResultCallBack syncCallBack) {
        String newFromDate = convertUTCToISO(fromDate);
        this.sync(null);
        syncParams.put("start_from", newFromDate);
        this.requestSync(syncCallBack);
    }

    protected String convertUTCToISO(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(tz);
        return dateFormat.format(date);
    }

    /**
     * Sync content type.
     *
     * @param contentType  Provide uid of your content_type
     * @param syncCallBack Returns callback for sync result.
     *                     <p>
     *                     You can also initialize sync with entries of only
     *                     specific content_type. To do this, use syncContentType
     *                     and specify the content type uid as its value. However,
     *                     if you do this, the subsequent syncs will only include
     *                     the entries of the specified content_type. <br>
     *                     <br>
     *                     <b>Example :</b>
     *                     <p>
     *                     stack.syncContentType(String content_type, new
     *                     SyncResultCallBack()){ }
     *
     */
    public void syncContentType(@NotNull String contentType, SyncResultCallBack syncCallBack) {
        this.sync(null);
        syncParams.put(CONTENT_TYPE_UID, contentType);
        this.requestSync(syncCallBack);
    }

    /**
     * Sync locale.
     *
     * @param localeCode   Select the required locale code.
     * @param syncCallBack Returns callback for sync result.
     *                     <p>
     *                     You can also initialize sync with entries of only
     *                     specific locales. To do this, use syncLocale and specify
     *                     the locale code as its value. However, if you do this,
     *                     the subsequent syncs will only include the entries of the
     *                     specified locales. <br>
     *                     <br>
     *                     <b>Example :</b><br>
     *
     *
     *
     */
    public void syncLocale(String localeCode, SyncResultCallBack syncCallBack) {
        this.sync(null);
        syncParams.put("locale", localeCode);
        this.requestSync(syncCallBack);
    }

    /**
     * Sync publish type.
     *
     * @param publishType  Use the type parameter to get a specific type of content
     *                     like
     *                     <p>
     *                     (asset_published, entry_published, asset_unpublished,
     *                     asset_deleted, entry_unpublished, entry_deleted,
     *                     content_type_deleted.)
     * @param syncCallBack returns callback for sync result.
     *                     <p>
     *                     Use the type parameter to get a specific type of content.
     *                     You can pass one of the following values:
     *                     asset_published, entry_published, asset_unpublished,
     *                     asset_deleted, entry_unpublished, entry_deleted,
     *                     content_type_deleted. If you do not specify any value, it
     *                     will bring all published entries and published assets.
     *                     <br>
     *                     <br>
     *                     <b>Example :</b><br>
     *
     *                     <pre class="prettyprint">
     *                     stackInstance.syncPublishType(Stack.PublishType.entry_published, new SyncResultCallBack()) { }
     *                     </pre>
     */
    public void syncPublishType(PublishType publishType, SyncResultCallBack syncCallBack) {
        this.sync(null);
        syncParams.put("type", publishType.name());
        this.requestSync(syncCallBack);
    }

    /**
     * Sync.
     *
     * @param contentType  your content type id
     * @param fromDate     start date
     * @param localeCode   language as language code
     * @param publishType  type as PublishType
     * @param syncCallBack Callback
     *                     <p>
     *                     You can also initialize sync with entries that satisfy
     *                     multiple parameters. To do this, use syncWith and specify
     *                     the parameters. However, if you do this, the subsequent
     *                     syncs will only include the entries of the specified
     *                     parameters <br>
     *                     <br>
     *                     <b>Example :</b><br>
     *
     *
     */
    public void sync(
            String contentType, Date fromDate, String localeCode, PublishType publishType, SyncResultCallBack syncCallBack) {
        String newDate = convertUTCToISO(fromDate);
        this.sync(null);
        syncParams.put("start_from", newDate);
        syncParams.put("content_type_uid", contentType);
        syncParams.put("type", publishType.name());
        syncParams.put("locale", localeCode);
        this.requestSync(syncCallBack);
    }

    private void requestSync(final SyncResultCallBack callback) {
        if (this.headers.containsKey(ENVIRONMENT)) {
            syncParams.put(ENVIRONMENT, this.headers.get(ENVIRONMENT));
        }
        fetchFromNetwork(SYNCHRONISATION, syncParams, this.headers, callback);
    }

    private void fetchContentTypes(String urlString, JSONObject contentTypeParam, HashMap<String, Object> headers,
            ContentTypesCallback callback) {
        if (callback != null) {
            HashMap<String, Object> queryParam = getUrlParams(contentTypeParam);
            String requestInfo = REQUEST_CONTROLLER.CONTENTTYPES.toString();
            new CSBackgroundTask(this, Constants.FETCHCONTENTTYPES, urlString, headers, queryParam, requestInfo,
                    callback);
        }
    }

    private void fetchFromNetwork(String urlString, JSONObject urlQueries, HashMap<String, Object> headers,
            SyncResultCallBack callback) {
        if (callback != null) {
            HashMap<String, Object> urlParams = getUrlParams(urlQueries);
            String requestInfo = REQUEST_CONTROLLER.SYNC.toString();
            new CSBackgroundTask(this, Constants.FETCHSYNC, urlString, headers, urlParams, requestInfo, callback);
        }
    }

    private HashMap<String, Object> getUrlParams(JSONObject jsonQuery) {
        HashMap<String, Object> hashMap = new HashMap<>();
        if (jsonQuery != null && jsonQuery.length() > 0) {
            Iterator<String> iteString = jsonQuery.keys();
            while (iteString.hasNext()) {
                String key = iteString.next();
                Object value = jsonQuery.opt(key);
                hashMap.put(key, value);
            }
        }
        return hashMap;
    }

    /**
     * The enum Publish type.
     */
    public enum PublishType {
        asset_deleted, asset_published, asset_unpublished, content_type_deleted, entry_deleted, entry_published,
        entry_unpublished
    }

}
