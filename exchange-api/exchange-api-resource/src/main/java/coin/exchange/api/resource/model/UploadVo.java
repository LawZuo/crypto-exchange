package coin.exchange.api.resource.model;

import lombok.Data;

@Data
public class UploadVo {

    private String url; // 图片地址
    private String name; // 图片名称
    private String type; // 图片类型
}
