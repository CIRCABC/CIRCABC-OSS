package com.sii.eucaptcha.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

@Service("redisStorage")
@Slf4j
public class CaptchaRedisStorageService implements CaptchaStorage {

  @Autowired
  private Environment environment;

  @Value("${expiry.time.in.seconds}")
  private long captchaExpiryTime;

  private JedisPooled jedis;

  @PostConstruct
  public void init() {
    String redisServer = environment.getProperty("REDIS");
    String redisPort = environment.getProperty("REDIS_PORT");
    String redisPassword = environment.getProperty("REDIS_PWD");

    if (redisServer == null) {
      redisServer = "localhost";
    }

    if (redisPort == null) {
      redisPort = "6379";
    }

    if (redisPassword == null) {
      this.jedis = new JedisPooled(redisServer, Integer.valueOf(redisPort));
    } else {
      this.jedis = new JedisPooled(
        redisServer,
        Integer.valueOf(redisPort),
        null,
        redisPassword
      );
    }
  }

  @Override
  public boolean validateCaptcha(
    String captchaId,
    String captchaAnswer,
    boolean usingAudio
  ) {
    boolean result = false;

    String answer = jedis.get(captchaId);

    if (answer != null) {
      log.debug(
        "Given answer is {}, stored answer is {}",
        captchaAnswer,
        answer
      );
      // case sensitive
      answer = StringUtils.deleteWhitespace(answer);
      if (!usingAudio) {
        result = answer.equals(captchaAnswer);
      }
      // if the audio is selected , ignore case sensitive
      else {
        result = answer.equalsIgnoreCase(captchaAnswer);
      }
      removeCaptcha(captchaId);
    }
    return result;
  }

  @Override
  public boolean validateWhatsUpCaptcha(
    String captchaId,
    String captchaAnswer
  ) {
    String storedAnswer = jedis.get(captchaId);
    if (storedAnswer != null) {
      removeCaptcha(captchaId);
      int storedAnswerAsInt = Integer.parseInt(storedAnswer);
      int givenAnswer = Integer.parseInt(captchaAnswer);

      log.debug(
        "stored answer = , givenAnswer = " + storedAnswerAsInt,
        givenAnswer
      );

      return (
        (givenAnswer == (storedAnswerAsInt * -1)) ||
        ((givenAnswer <= 0)
            ? ((givenAnswer * -1 - 360) == storedAnswerAsInt)
            : ((360 - givenAnswer) == storedAnswerAsInt))
      );
    } else {
      return false;
    }
  }

  @Override
  public void addCaptcha(String captchaId, String captchaAnswer) {
    jedis.setex(captchaId, captchaExpiryTime, captchaAnswer);
  }

  @Override
  public void removeCaptcha(String captchaId) {
    jedis.del(captchaId);
  }
}
