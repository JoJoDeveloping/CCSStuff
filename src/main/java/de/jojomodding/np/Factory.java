package de.jojomodding.np;

import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.ccs.expr.Choice;
import de.jojomodding.np.ccs.expr.FilterSet;
import de.jojomodding.np.ccs.expr.Parallel;
import de.jojomodding.np.ccs.expr.Prefix;
import de.jojomodding.np.ccs.expr.Restrict;
import de.jojomodding.np.ccs.expr.Stop;
import de.jojomodding.np.ccs.expr.Variable;
import de.jojomodding.np.lts.Action;
import de.jojomodding.np.lts.Channel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Factory {

    private Factory() {}

    public static Choice choice(String l, CCSExpression r) {
        return new Choice(var(l), r);
    }

    public static Choice choice(CCSExpression l, String r) {
        return new Choice(l, var(r));
    }

    public static Choice choice(String l, String r) {
        return new Choice(var(l), var(r));
    }

    public static CCSExpression choice(CCSExpression... base) {
        if (base.length == 0)
            return stop();
        if (base.length == 1)
            return base[0];
        CCSExpression exp = base[0];
        for (int i = 1; i < base.length; i++)
            exp = new Choice(exp, base[i]);
        return exp;
    }

    public static CCSExpression choice(List<CCSExpression> base) {
        if (base.size() == 0)
            return stop();
        if (base.size() == 1)
            return base.get(0);
        Iterator<CCSExpression> iter = base.iterator();
        CCSExpression exp = iter.next();
        while (iter.hasNext()) {
            exp = new Choice(exp, iter.next());
        }
        return exp;
    }

    public static Collector<CCSExpression, ?, CCSExpression> toChoice() {
        return Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Factory::choice);
    }

    public static Parallel parallel(String lvar, CCSExpression r) {
        return new Parallel(var(lvar), r);
    }

    public static Parallel parallel(CCSExpression l, String rvar) {
        return new Parallel(l, var(rvar));
    }

    public static Parallel parallel(String lvar, String rvar) {
        return new Parallel(var(lvar), var(rvar));
    }

    public static CCSExpression parallel(CCSExpression... base) {
        if (base.length == 0)
            return stop();
        if (base.length == 1)
            return base[0];
        CCSExpression exp = base[0];
        for (int i = 1; i < base.length; i++)
            exp = new Parallel(exp, base[i]);
        return exp;
    }

    public static CCSExpression parallel(List<CCSExpression> base) {
        if (base.size() == 0)
            return stop();
        if (base.size() == 1)
            return base.get(0);
        Iterator<CCSExpression> iter = base.iterator();
        CCSExpression exp = iter.next();
        while (iter.hasNext()) {
            exp = new Parallel(exp, iter.next());
        }
        return exp;
    }

    public static Collector<CCSExpression, ?, CCSExpression> toParallel() {
        return Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Factory::parallel);
    }

    public static Restrict restrict(CCSExpression l, FilterSet r) {
        return new Restrict(l, r);
    }

    public static Restrict restrict(String var, FilterSet r) {
        return new Restrict(var(var), r);
    }

    public static Prefix prefix(Action a, CCSExpression e) {
        return new Prefix(a, e);
    }

    public static Prefix prefix(Action a, String var) {
        return new Prefix(a, var(var));
    }

    public static Stop stop() {
        return Stop.instance();
    }

    public static Variable var(String s) {
        return new Variable(s);
    }


    public static Action.InternalAction tau() {
        return Action.tau();
    }

    public static Action.SendingAction sending(Channel c) {
        return Action.sending(c);
    }

    public static Action.SendingAction sending(String s) {
        return sending(of(s));
    }

    public static Action.ReceivingAction receiving(Channel c) {
        return Action.receiving(c);
    }

    public static Action.ReceivingAction receiving(String s) {
        return receiving(of(s));
    }

    public static Channel of(String name) {
        return new Channel(name);
    }

    public static FilterSet excluding(Channel... c) {
        return new FilterSet(true, Set.of(c));
    }

    public static FilterSet excluding(String... c) {
        return new FilterSet(true, Arrays.stream(c).map(Factory::of).collect(Collectors.toUnmodifiableSet()));
    }

    public static FilterSet including(Channel... c) {
        return new FilterSet(false, Set.of(c));
    }

    public static FilterSet including(String... c) {
        return new FilterSet(false, Arrays.stream(c).map(Factory::of).collect(Collectors.toUnmodifiableSet()));
    }

}
