package coin.exchange.module.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.user.dto.KycApplicationDto;
import coin.exchange.module.user.domain.KycApplicationDo;
import coin.exchange.module.user.mapper.KycApplicationMapper;
import coin.exchange.module.user.service.KycApplicationService;
import coin.exchange.module.user.utils.ValidationUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KycApplicationServiceImpl implements KycApplicationService {

    private final KycApplicationMapper kycApplicationMapper;

    @Override
    public Long createKycApplication(KycApplicationDto kycApplication) {
        // 越界判断
        ValidationUtil.validateKycApplication(kycApplication);

        KycApplicationDo domain = new KycApplicationDo();
        BeanUtil.copyProperties(kycApplication, domain, "id", "createdTime", "updateTime", "isDeleted");

        Long id = (long) kycApplicationMapper.insert(domain);
        return id;
    }

    @Override
    public Long updateKycApplication(Long id, KycApplicationDto kycApplication) {
        // 越界判断
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        ValidationUtil.validateKycApplication(kycApplication);

        KycApplicationDo domain = new KycApplicationDo();
        BeanUtil.copyProperties(kycApplication, domain, "createdTime", "isDeleted");

        kycApplicationMapper.updateById(domain);
        return id;
    }

    @Override
    public Long deleteKycApplication(Long id) {
        // 越界判断
        if (id == null) {
            throw new RuntimeException("记录ID不能为空");
        }
        return (long) kycApplicationMapper.deleteById(id);
    }

    @Override
    public KycApplicationDo getKycApplication(Long userId) {
        if (userId == null) {
            throw new RuntimeException("记录ID不能为空");
        }
        return kycApplicationMapper.getKycApplication(userId);
    }

    @Override
    public void updateStatus(Long id, int status) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        LambdaUpdateWrapper<KycApplicationDo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KycApplicationDo::getId, id)
                .set(KycApplicationDo::getStatus, status);
        kycApplicationMapper.update(null, updateWrapper);
    }
}
