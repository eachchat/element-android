package im.vector.app.yiqia.contact.api.bean;

import java.io.Serializable;

/**
 * Created by chengww on 2019-09-27.
 */
public class AvatarUploadBean implements Serializable {

    /**
     * ext : png
     * fileSize : 344613
     * fileName : SHyxIdRPzTZKuPqdGuId.png
     * url : http://store.jdbmdm.com:8899/group1/M00/00/09/wKgABV3U6DqAfbW7AAVCJeZF6MQ634.png
     * middleImage : http://store.jdbmdm.com:8899/group1/M00/00/09/wKgABV3U6DqAILGNAAHAg4I0NIc665.png
     * thumbnailImage : http://store.jdbmdm.com:8899/group1/M00/00/09/wKgABV3U6DqAILGNAAHAg4I0NIc665.png
     * imgWidth : 512
     * imgHeight : 512
     */

    private String ext;
    private long fileSize;
    private String fileName;
    private String url;
    private String middleImage;
    private String thumbnailImage;
    private int imgWidth;
    private int imgHeight;

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMiddleImage() {
        return middleImage;
    }

    public void setMiddleImage(String middleImage) {
        this.middleImage = middleImage;
    }

    public String getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(String thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }
}
