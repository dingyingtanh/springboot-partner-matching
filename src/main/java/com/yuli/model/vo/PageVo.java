package com.yuli.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "分页对象", description = "分页返回分页对象")
public class PageVo {

    @ApiModelProperty(value = "当前页码", required = true)
    private Long currentPage;
    @ApiModelProperty(value = "每页显示数量", required = true)
    private Long pageSize;
    @ApiModelProperty(value = "总页数", required = true)
    private Long pageTotal;
    @ApiModelProperty(value = "总记录数", required = true)
    private List records;
}
