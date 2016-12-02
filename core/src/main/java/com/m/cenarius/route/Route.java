package com.m.cenarius.route;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * Route负责通过uri找到对应的html页面，一条Route包含一个uri和一个hash值。
 */
public class Route implements Serializable{

    private static final long serialVersionUID = 1l;
    @JSONField(name="hash")
    public String fileHash;
    @JSONField(name="file")
    public String uri;
//    @SerializedName("remoteHTML")
//    public String remoteHTML;

    public Route() {
    }

    /**
     * 匹配传入的uri，如果能匹配上则说明可以用这个html来显示
     *
     * @param uri 匹配的uri
     * @return true: 能匹配上  false: 不能匹配上
     */
    public boolean match(String uri) {
        return uri.equals(this.uri);
    }

    /**
     * 返回html地址
     *
     * @return html的远程地址
     */
    public String getHtmlFile() {
        String remoteHTML = RouteManager.getInstance().remoteFolderUrl + "/" + uri;
        return remoteHTML;
    }

//    @Override
//    public int hashCode() {
//        return fileHash.hashCode();
//    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (!(o instanceof Route)) {
            return false;
        }
        return TextUtils.equals(this.fileHash, ((Route) o).fileHash);
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
