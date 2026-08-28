package coin.exchange.api.account.model;

import lombok.Data;

@Data
public class AccountWalletVo {
    private Long id;
    private Long userId;
    private String currency;
    private Integer walletType;
    private String balance;
    private String freezeBalance;
    private String availableBalance;
    private String totalBalance;
    private String address;
    private String network;
    private String createTime;
    private String updateTime;
    private String remark;
    private String status;
}
