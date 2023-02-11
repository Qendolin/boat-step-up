package com.qendolin.boatstepup.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigScreen<C extends Config> extends Screen {
    private final Screen parent;
    private final C config;
    private final String translationKeyPrefix;
    protected final Map<String, ConfigEntry> entries = new HashMap<>();
    private final CloseAction<C> onClose;
    protected ButtonWidget doneButton;

    protected static Map<Class<? extends Annotation>, WidgetFactory<? extends Annotation>> annotationWidgetFactories;

    static {
        annotationWidgetFactories = new HashMap<>();
        Entry.init();
    }

    public interface WidgetFactory<A extends Annotation> {
        ClickableWidget create(A a, int x, int y, int w, int h, Field f, Object o, Function<?, String> s) throws IllegalAccessException;
    }

    public ConfigScreen(Screen parent, C config) {
        super(Text.translatable(config.getId()+".config.title"));
        this.parent = parent;
        this.config = config;
        this.onClose = ConfigScreen::onCloseDefault;
        translationKeyPrefix = config.getId()+".config.entry.";
    }

    public ConfigScreen(Screen parent, C config, CloseAction<C> onClose) {
        super(Text.translatable(config.getId() + ".config.title"));
        this.parent = parent;
        this.config = config;
        this.onClose = onClose;
        translationKeyPrefix = config.getId() + ".config.entry.";
    }

    public static <C extends Config> void onCloseDefault(boolean save, C config, List<EntryValueSetter<?>> valueSetters) {
        if (!save) return;
        for (EntryValueSetter<?> setter : valueSetters) {
            setter.apply(config);
        }
        ConfigManager.save(config);
    }

    public static <T extends Annotation> void registerWidgetFactory(Class<T> annotation, WidgetFactory<T> factory) {
        ConfigScreen.annotationWidgetFactories.put(annotation, factory);
    }

    private <T> Function<T, String> getStringer(String name) {
        Method stringer = null;
        for (Method method : config.getClass().getDeclaredMethods()) {
            if(method.getName().equals(name)) {
                stringer = method;
                break;
            }
        }
        if(stringer == null) {
            ConfigManager.LOGGER.error(new NoSuchMethodException(config.getClass().getName() + "." + name + "(String)"));
            return null;
        }
        final Method finalStringer = stringer;
        finalStringer.setAccessible(true);
        return o -> {
            try {
                return (String) finalStringer.invoke(config, o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                ConfigManager.LOGGER.error(e);
                return Objects.toString(o);
            }
        };
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, textRenderer, title, width/2, textRenderer.fontHeight, 0xFFFFFF);
        int sx = 20;
        for (ConfigEntry entry : entries.values()) {
            int centerY = entry.y + entry.height / 2 - textRenderer.fontHeight / 2;
            Text text = Text.translatable(translationKeyPrefix + entry.name);
            drawTextWithShadow(matrices, textRenderer, text, sx, centerY, 0xffffff);

            String tooltipKey = translationKeyPrefix + entry.name + ".tooltip";
            if(mouseX > sx && mouseX < sx + textRenderer.getWidth(text) && mouseY > entry.y && mouseY < entry.y + entry.height) {
                renderTooltip(matrices, Text.translatable(tooltipKey), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        final List<EntryValueSetter<?>> valueSetters = new ArrayList<>();

        doneButton = addDrawableChild(new ButtonWidget(this.width/2 + 4,this.height - 20 - 8,150,20, Text.translatable("gui.done"), (button) -> {
            client.setScreen(parent);
            onClose.invoke(true, this.config, valueSetters);
        }));
        addDrawableChild(new ButtonWidget(this.width/2 - 150 - 4,this.height - 20 - 8,150,20, Text.translatable("gui.cancel"), (button) -> {
            client.setScreen(parent);
            onClose.invoke(false, this.config, valueSetters);
        }));

        int sx = width-200-20;
        int sy = 40;
        try {
            for (Field field : config.getClass().getFields()) {
                for (Annotation annotation : field.getAnnotations()) {
                    WidgetFactory provider = annotationWidgetFactories.getOrDefault(annotation.annotationType(), null);
                    if(provider == null) continue;
                    String stringerName = (String) annotation.annotationType().getMethod("stringer").invoke(annotation);
                    ClickableWidget widget = provider.create(annotation, sx, sy, 200, 20, field, config, stringerName.isEmpty() ? null : getStringer(stringerName));

                    entries.put(field.getName(), new ConfigEntry(field.getName(), field, widget.y, widget.x, widget.getHeight(), widget.getWidth(), widget));
                    valueSetters.add(new EntryValueSetter<>(field, ((ValueHolder<?>) widget)::getValue));
                    addDrawableChild(widget);
                    sy += widget.getHeight() + 2;
                }
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            ConfigManager.LOGGER.fatal(e);
        }
    }

    protected static record ConfigEntry(String name, Field field, int y, int x, int height, int width, ClickableWidget widget) {}
    public static record EntryValueSetter<V>(Field field, Supplier<V> valueSupplier){
        public <T extends Config> void apply(T config) {
            try {
                field.set(config, valueSupplier.get());
            } catch(IllegalArgumentException | IllegalAccessException e) {
                ConfigManager.LOGGER.error(e);
            }
        }
    }

    public interface CloseAction<C extends Config> {
        void invoke(boolean save, C config, List<EntryValueSetter<?>> valueSetters);
    }
}