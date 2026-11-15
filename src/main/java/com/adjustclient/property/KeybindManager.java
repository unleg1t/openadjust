package com.adjustclient.property;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindAction;
import com.adjustclient.bind.BindKey;
import com.adjustclient.bind.BindMode;
import com.adjustclient.bind.KeyboardKey;
import com.adjustclient.bind.MouseKey;
import com.adjustclient.bind.ScrollKey;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.KeyBindingUpdateEvent;
import com.adjustclient.event.Listener;
import com.adjustclient.event.MouseButtonInputEvent;
import com.adjustclient.event.ScreenKeyEvent;
import com.adjustclient.util.ScreenUtil;
import com.adjustclient.vfs.VirtualFileSystem;

public final class KeybindManager extends Manager<Bind> implements Listener {
    public static boolean scrollUp;
    public static boolean scrollDown;
    public Object field16;

    public Bind getByHash(int hash) {
        for (Bind bind : view) if (bind.id == hash) return bind;
        return null;
    }

    public void dispatchKey(KeyboardKey pressed) {
        for (Bind bind : view) {
            for (BindKey key : bind.keys) {
                if (key instanceof KeyboardKey && ((KeyboardKey) key).keyCode == pressed.keyCode) {
                    bind.action.onAction(BindMode.RELEASED);
                }
            }
        }
    }

    @EventHandler
    public void onScreenKey(ScreenKeyEvent event) {
        if (!event.pressed) return;
        for (Bind bind : view) {
            if (!bind.down) continue;
            if (ScreenUtil.canMoveInScreen()) return;
            for (BindKey key : bind.keys) {
                if (key instanceof KeyboardKey && ((KeyboardKey) key).keyCode == event.keyCode) {
                    bind.action.onAction(BindMode.PRESSED);
                    event.cancelled = true;
                }
            }
        }
    }

    public void onScroll(int delta) {
        ScrollKey scrollKey = ScrollKey.fromWheel(delta);
        if (scrollKey == null) return;
        if (delta >= 1) scrollUp = true;
        else if (delta < 0) scrollDown = true;
        for (Bind bind : view) {
            if (bind.keys.contains(scrollKey)) {
                bind.action.onAction(BindMode.PRESSED);
            }
        }
    }

    public void dispatchScroll() {
        for (Bind bind : view) {
            for (BindKey key : bind.keys) {
                if (key instanceof ScrollKey) {
                    if (((ScrollKey) key).keyCode() == 1069 ? scrollUp : scrollDown) {
                        bind.action.onAction(BindMode.RELEASED);
                    }
                }
            }
        }
        scrollUp = false;
        scrollDown = false;
    }

    @EventHandler(priority = -100)
    public void onMouseButton(MouseButtonInputEvent event) {
        if (event.isCancelled() || event.button < 0) return;
        if (event.down && ScreenUtil.isAdjustScreenOpen()) return;
        BindMode mode = event.down ? BindMode.PRESSED : BindMode.RELEASED;
        for (Bind bind : view) {
            for (BindKey key : bind.keys) {
                if (key instanceof MouseKey && ((MouseKey) key).button == event.button) {
                    bind.action.onAction(mode);
                }
            }
        }
    }

    @EventHandler(priority = -100)
    public void onKeyBindingUpdate(KeyBindingUpdateEvent event) {
        if (event.isCancelled()) return;
        for (Bind bind : view) {
            for (BindKey key : bind.keys) {
                if (key instanceof KeyboardKey && ((KeyboardKey) key).keyCode == event.keyCode) {
                    bind.action.onAction(BindMode.PRESSED);
                }
            }
        }
    }

    public int clearKeys() {
        if (view.isEmpty()) return 0;
        int count = 0;
        for (Bind bind : view) {
            if (!bind.mutable) continue;
            BindKey[] keys = bind.keys.toArray(new BindKey[0]);
            for (BindKey key : keys) {
                if (bind.mutable) bind.removeKey(key);
            }
            count += keys.length;
        }
        ((VirtualFileSystem) Adjust.INSTANCE.fileSystem).markDirty();
        return count;
    }

    public Bind getByName(String name) {
        for (Bind bind : view) {
            if (bind.name != null && !bind.hidden
                    && (name.equalsIgnoreCase(bind.name)
                    || name.equalsIgnoreCase(bind.name.replace(" ", "")))) return bind;
        }
        return null;
    }

    @Override
    protected boolean matches(Bind bind, Object key) { return false; }

    public void checkCollision(String id, int hash, String name) {
        if (getByHash(hash) != null) throw new RuntimeException("Collision: " + id);
        if (name != null && getByName(name) != null) {
            throw new RuntimeException("Name collision: " + name + " with " + id);
        }
    }

    public Bind register(String id, BindAction action, String name) {
        if (action == null) action = mode -> {
        };
        int hash = hashId(id);
        checkCollision(id, hash, name);
        Bind bind = new Bind(hash, action, name);
        add(bind);
        return bind;
    }

    public static int hashId(String id) {
        int hash = 31;
        for (char c : id.toCharArray()) hash = 31 * hash + c;
        return hash;
    }
}
