package com.qendolin.boatstepup.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

public abstract class Entry {
    static void init() {
        ConfigScreen.registerWidgetFactory(IntRange.class, (a, x, y, w, h, f, o, s) -> Widgets.intRange(x, y, w, a.min(), a.max(), f.getInt(o), (Function<Integer, String>) s));
        ConfigScreen.registerWidgetFactory(FloatRange.class, (a, x, y, w, h, f, o, s) -> Widgets.floatRange(x, y, w, a.min(), a.max(), a.step(), f.getFloat(o), (Function<Float, String>) s));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IntRange {
        int min() default 0;
        int max();
        String stringer() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FloatRange {
        float min() default 0f;
        float max() default 1f;
        float step() default 0f;
        String stringer() default "";
    }
}
