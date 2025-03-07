package me.sonam.siteaccess.springboot;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeDiffTest {
    private static final Logger LOG = LoggerFactory.getLogger(TimeDiffTest.class);

    @Test
    public void durationTestPass() throws  Exception {
        LocalDateTime originalDateTime = LocalDateTime.now();
        LOG.info("sleep for 4 seconds");
        Thread.sleep(4000);
        LOG.info("wakeup now");
        LocalDateTime after = LocalDateTime.now();

        Duration duration = Duration.between(originalDateTime, after);
        if( duration.getSeconds() >= 4) {
            LOG.info("duration is greater than 4 seconds");
            assertThat(duration.getSeconds()).isGreaterThan(3);
        }
        else {
            LOG.error("duration is LESS, error, than 4 seconds");
            fail();
        }
    }

    @Test
    public void durationTestFail() throws  Exception {
        LocalDateTime originalDateTime = LocalDateTime.now();
        LOG.info("sleep for 4 seconds");
        Thread.sleep(1000);
        LOG.info("wakeup now");
        LocalDateTime after = LocalDateTime.now();

        Duration duration = Duration.between(originalDateTime, after);
        if( duration.getSeconds() <= 4) {
            LOG.info("duration is LESS than 4 seconds");
            assertThat(duration.getSeconds()).isLessThan(3);
        }
        else {
            LOG.error("duration is GREATER than 4 seconds");
            fail();
        }
    }

}
