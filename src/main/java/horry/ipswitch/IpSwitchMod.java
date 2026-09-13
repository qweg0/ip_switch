package horry.ipswitch;

import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

public class IpSwitchMenu {

    public static void show() {
        BaseDialog dialog = new BaseDialog("IP Switch");
        dialog.cont.pane(t -> buildList(t, dialog)).size(720f, 420f).row();

        dialog.cont.table(btns -> {
            btns.defaults().pad(6f).width(220f).height(60f);
            btns.button("Добавить прокси", Icon.add, () -> showAdd(dialog));
            btns.button("Закрыть", Icon.cancel, dialog::hide);
        }).pad(8f);

        dialog.addCloseButton();
        dialog.show();
    }

    private static void buildList(Table t, BaseDialog dialog) {
        t.clear();
        t.top().left();
        t.defaults().pad(6f).left();

        ProxyConfig.Proxy active = IpSwitchMod.config.active();

        t.table(st -> {
            st.left().defaults().pad(6f).left();

            st.add("[lightgray]Статус:[] ").left();
            if (active == null) {
                st.add("[scarlet]обычный IP[]").left();
            } else {
                st.add("[lime]активен[] ").left();
                st.add("[white]" + active.name + "[] ").left();
                st.add("[lightgray](" + active.host + ":" + active.port + ")[]").left();
            }
            st.row();

            st.add("[lightgray]Смена UUID:[] ").left();
            st.add(IpSwitchMod.config.spoofUuid ? "[lime]ВКЛ[]" : "[scarlet]ВЫКЛ[]").left();
        }).growX().row();

        t.image().height(2f).growX().pad(4f).row();

        if (IpSwitchMod.config.proxies.isEmpty()) {
            t.add("[lightgray]Прокси не добавлены.[]").pad(20f).row();
            return;
        }

        t.add("[lightgray]Профили:[]").left().row();

        for (ProxyConfig.Proxy p : IpSwitchMod.config.list()) {
            t.table(row -> {
                row.left().defaults().pad(6f).left();

                row.add(p.enabled ? "[lime]●[]" : "[darkgray]○[]").width(28f);

                row.table(info -> {
                    info.left().defaults().pad(2f).left();
                    info.add("[white]" + p.name + "[]").row();
                    info.add("[lightgray]" + p.host + ":" + p.port
                        + (p.user.isEmpty() ? "" : " (auth)") + "[]").row();
                }).growX();

                row.button(p.enabled ? "выкл" : "вкл", Icon.play, () -> {
                    if (p.enabled) {
                        IpSwitchMod.config.deactivate();
                    } else {
                        IpSwitchMod.config.activate(p.name);
                        SocketHook.install(IpSwitchMod.config);
                    }
                    dialog.hide();
                    show();
                }).width(120f).height(50f);

                row.button("удалить", Icon.trash, () -> {
                    IpSwitchMod.config.remove(p.name);
                    dialog.hide();
                    show();
                }).width(130f).height(50f);
            }).growX().pad(2f).row();
        }

        t.image().height(2f).growX().pad(4f).row();

        t.button(IpSwitchMod.config.spoofUuid
                ? "Выключить смену UUID"
                : "Включить смену UUID",
            Icon.refresh, () -> {
                IpSwitchMod.config.spoofUuid = !IpSwitchMod.config.spoofUuid;
                IpSwitchMod.config.save();
                if (IpSwitchMod.config.spoofUuid) {
                    UuidSwapper.rotate();
                } else {
                    UuidSwapper.reset();
                }
                dialog.hide();
                show();
            }).width(400f).height(60f).pad(6f).row();
    }

    private static void showAdd(BaseDialog parent) {
        BaseDialog add = new BaseDialog("Новый прокси");
        add.cont.defaults().pad(6f).left();

        add.cont.add("[lightgray]Имя профиля:[]").left().row();
        TextField nameField = new TextField("");
        nameField.setMessageText("main");
        add.cont.add(nameField).width(600f).row();

        add.cont.add("[lightgray]Хост (IP или домен):[]").left().row();
        TextField hostField = new TextField("");
        hostField.setMessageText("1.2.3.4");
        add.cont.add(hostField).width(600f).row();

        add.cont.add("[lightgray]Порт:[]").left().row();
        TextField portField = new TextField("1080");
        add.cont.add(portField).width(600f).row();

        add.cont.add("[lightgray]Логин (если нужен):[]").left().row();
        TextField userField = new TextField("");
        add.cont.add(userField).width(600f).row();

        add.cont.add("[lightgray]Пароль (если нужен):[]").left().row();
        TextField passField = new TextField("");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        add.cont.add(passField).width(600f).row();

        add.cont.table(btns -> {
            btns.defaults().pad(6f).width(200f).height(60f);

            btns.button("Отмена", Icon.cancel, add::hide);

            btns.button("Добавить", Icon.ok, () -> {
                String name = nameField.getText().trim();
                String host = hostField.getText().trim();
                String portStr = portField.getText().trim();
                String user = userField.getText().trim();
                String pass = passField.getText();

                if (name.isEmpty() || host.isEmpty()) {
                    Log.err("[IP Switch] имя и хост обязательны");
                    return;
                }

                int port;
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    Log.err("[IP Switch] порт не число: @", portStr);
                    return;
                }

                IpSwitchMod.config.add(name, host, port, user, pass);
                add.hide();
                parent.hide();
                show();
            });
        }).pad(8f);

        add.addCloseButton();
        add.show();
    }
}
