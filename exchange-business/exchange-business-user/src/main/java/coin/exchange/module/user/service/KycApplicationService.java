package coin.exchange.module.user.service;

import coin.exchange.api.user.dto.KycApplicationDto;
import coin.exchange.module.user.domain.KycApplicationDo;

public interface KycApplicationService {

    /**
     * create kyc BY userId
     */
    Long createKycApplication(KycApplicationDto kycApplication);

    /**
     * update kyc By userId
     */
    Long updateKycApplication(Long id, KycApplicationDto kycApplication);

    /**
     * delete kyc By userId
     */
    Long deleteKycApplication(Long id);

    /**
     * get kyc By userId
     */
    KycApplicationDo getKycApplication(Long userId);

    /**
     *  update status
     */
    void updateStatus(Long id, int status);
}
