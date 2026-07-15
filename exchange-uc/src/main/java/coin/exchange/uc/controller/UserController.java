package coin.exchange.uc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/crypto-exchange/user")
@RestController
@Slf4j
public class UserController {

    @GetMapping("/test")
    public String test() {
        log.info("UserController.test()");
        return "UserController.test()";
    }
}
