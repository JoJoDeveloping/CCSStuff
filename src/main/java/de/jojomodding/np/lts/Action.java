package de.jojomodding.np.lts;

import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.Factory;

import java.util.Objects;
import java.util.Optional;


public abstract class Action {

    public boolean isInternal() {
        return false;
    }

    public boolean isSending() {
        return false;
    }

    public boolean isReceiving() {
        return false;
    }

    public Optional<Channel> getChannel() {
        return Optional.empty();
    }

    public boolean isInverseOf(Action a) {
        return false;
    }

    public abstract Action inverse();

    @Override
    public abstract String toString();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract int hashCode();

    public static class InternalAction extends Action {

        private static final InternalAction theTau = new InternalAction();

        private InternalAction() {

        }

        @Override
        public boolean isInternal() {
            return true;
        }

        @Override
        public String toString() {
            return "τ";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof InternalAction;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public Action inverse() {
            return this;
        }
    }

    public static class SendingAction extends Action {

        private final Channel chan;

        private SendingAction(Channel chan) {
            this.chan = chan;
        }

        @Override
        public boolean isSending() {
            return true;
        }

        @Override
        public Optional<Channel> getChannel() {
            return Optional.of(chan);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SendingAction that = (SendingAction) o;
            return chan.equals(that.chan);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chan);
        }

        @Override
        public String toString() {
            return chan.toString() + "!";
        }

        @Override
        public boolean isInverseOf(Action a) {
            if (!(a instanceof ReceivingAction))
                return false;
            return ((ReceivingAction) a).chan.equals(chan);
        }

        @Override
        public Action inverse() {
            return new ReceivingAction(chan);
        }
    }


    public static class ReceivingAction extends Action {

        private final Channel chan;

        private ReceivingAction(Channel chan) {
            this.chan = chan;
        }

        @Override
        public boolean isReceiving() {
            return true;
        }

        @Override
        public Optional<Channel> getChannel() {
            return Optional.of(chan);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReceivingAction that = (ReceivingAction) o;
            return chan.equals(that.chan);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chan);
        }

        @Override
        public String toString() {
            return chan.toString() + "?";
        }

        @Override
        public boolean isInverseOf(Action a) {
            if (!(a instanceof SendingAction))
                return false;
            return ((SendingAction) a).chan.equals(chan);
        }

        @Override
        public Action inverse() {
            return new SendingAction(chan);
        }
    }

    public static InternalAction tau() {
        return InternalAction.theTau;
    }

    public static Action.SendingAction sending(Channel c) {
        return new Action.SendingAction(c);
    }

    public static Action.ReceivingAction receiving(Channel c) {
        return new Action.ReceivingAction(c);
    }

    public CCSExpression then(CCSExpression then) {
        return Factory.prefix(this, then);
    }

    public CCSExpression then(String var) {
        return Factory.prefix(this, var);
    }
}
