package io.swagger.model;

import java.util.Objects;

public class Comment {

    private String text = null;

    /**
     * Get name
     *
     * @return name
     */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        return Objects.equals(this.text, comment.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
