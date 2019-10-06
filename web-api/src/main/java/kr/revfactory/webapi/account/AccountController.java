package kr.revfactory.webapi.account;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final RSocketRequester accountRSocketRequester;

    public void account() {
    }
}
