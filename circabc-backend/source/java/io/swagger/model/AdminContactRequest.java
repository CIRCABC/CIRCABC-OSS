/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class AdminContactRequest {

    private String content = null;
    private Boolean sendCopy = false;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getSendCopy() {
        return sendCopy;
    }

    public void setSendCopy(Boolean sendCopy) {
        this.sendCopy = sendCopy;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminContactRequest adminContactRequest = (AdminContactRequest) o;
        return Objects.equals(this.content, adminContactRequest.content)
                && Objects.equals(this.sendCopy, adminContactRequest.sendCopy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, sendCopy);
    }

    @Override
    public String toString() {

        return "class AdminContactRequest {\n"
                + "    content: "
                + Util.toIndentedString(content)
                + "\n"
                + "    sendCopy: "
                + Util.toIndentedString(sendCopy)
                + "\n}";
    }
}
