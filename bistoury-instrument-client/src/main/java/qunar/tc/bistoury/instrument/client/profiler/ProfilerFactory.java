package qunar.tc.bistoury.instrument.client.profiler;

import qunar.tc.bistoury.instrument.client.profiler.sampling.SamplingProfiler;

import java.util.Map;

/**
 * @author cai.wen created on 2019/10/23 11:13
 */
public class ProfilerFactory {

    private static final int DEFAULT_FREQUENCY_MIILIS = 10;

    private static final int DEFAULT_DURATION_SECONDS = 120;

    public static Profiler create(Mode mode, Map<String, Object> config) {
        mode = getMode(mode);
        switch (mode) {
            case sampler:
                return getSamplingProfiler(config);
            default:
                throw new RuntimeException("no kind of mode: " + mode);
        }
    }

    private static Mode getMode(Mode mode) {
        if (mode == null) {
            return Mode.sampler;
        }
        return mode;
    }

    private static Profiler getSamplingProfiler(Map<String, Object> config) {
        Integer frequencyMiilis = (Integer) config.get(ProfilerConstants.FREQUENCY);
        Integer durationSeconds = (Integer) config.get(ProfilerConstants.DURATION);
        frequencyMiilis = frequencyMiilis == null ? DEFAULT_FREQUENCY_MIILIS : frequencyMiilis;
        durationSeconds = durationSeconds == null ? DEFAULT_DURATION_SECONDS : durationSeconds;
        return new SamplingProfiler(durationSeconds, frequencyMiilis);
    }
}