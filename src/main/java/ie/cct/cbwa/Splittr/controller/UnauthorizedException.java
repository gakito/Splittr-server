package ie.cct.cbwa.Splittr.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Invalid username or password")
public class UnauthorizedException extends RuntimeException {
}
