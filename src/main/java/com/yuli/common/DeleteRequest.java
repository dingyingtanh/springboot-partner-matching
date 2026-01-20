package com.yuli.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -8982913243931934923L;

    private long id;
}
