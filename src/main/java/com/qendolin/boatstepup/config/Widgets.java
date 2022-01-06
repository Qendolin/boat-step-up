package com.qendolin.boatstepup.config;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.Function;

public class Widgets {
    public static RangeWidget<Integer> intRange(int x, int y, int width, int min, int max, int value, Function<Integer, String> messageMapper) {
        if(messageMapper == null) messageMapper = v -> String.format("%d", v);
        return new RangeWidget<>(x, y, width, value, v -> RangeWidget.mapIntToRange(v, min, max), messageMapper);
    }

    public static RangeWidget<Float> floatRange(int x, int y, int width, float min, float max, float step, float value, Function<Float, String> messageMapper) {
        if(messageMapper == null) messageMapper = v -> String.format("%.4f", v);
        return new RangeWidget<>(x, y, width, value, (v) -> RangeWidget.mapFloatToRange(v, min, max, step), messageMapper);
    }
}

interface ValueHolder<V> {
    V getValue();
}

class RangeWidget<T extends Number> extends SliderWidget implements ValueHolder<T> {
    private final Function<Double, T> valueMapper;
    private final Function<T, String> messageMapper;
    private T mappedValue;
    public RangeWidget(int x, int y, int width, T value, Function<Double, T> valueMapper, Function<T, String> messageMapper) {
        super(x, y, width, 20, null, 0);
        T min = valueMapper.apply(0d);
        T max = valueMapper.apply(1d);
        this.value = (value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue());

        this.valueMapper = valueMapper;
        this.messageMapper = messageMapper;
        applyValue();
        updateMessage();
    }

    public static Float mapFloatToRange(Double v, float min, float max, float step) {
        v = v * (max - min) + min;
        if(step != 0) {
            v = (double) Math.round(v / step) * step;
        }
        return (float) (double) v;
    }

    public static int mapIntToRange(Double v, int min, int max) {
        v = v * (max - min) + min;
        return (int) Math.round(v);
    }

    @Override
    protected void updateMessage() {
        setMessage(new LiteralText(this.messageMapper.apply(mappedValue)));
    }

    @Override
    protected void applyValue() {
        this.mappedValue = this.valueMapper.apply(this.value);
    }

    @Override
    public T getValue() {
        return mappedValue;
    }
}
