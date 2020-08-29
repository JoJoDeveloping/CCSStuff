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

/**
 * Constructs all kinds of CCS objects
 */
public final class Factory {

    private Factory() {
    }

    /**
     * Constructs the expression l+r
     */
    public static Choice choice(String l, CCSExpression r) {
        return new Choice(var(l), r);
    }

    /**
     * Constructs the expression l+r
     */
    public static Choice choice(CCSExpression l, String r) {
        return new Choice(l, var(r));
    }

    /**
     * Constructs the expression l+r
     */
    public static Choice choice(String l, String r) {
        return new Choice(var(l), var(r));
    }

    /**
     * Constructs the big choice between all expressions given
     */
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

    /**
     * Constructs the big choice between all expressions given
     */
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

    /**
     * Collects a stream into a big choice
     */
    public static Collector<CCSExpression, ?, CCSExpression> toChoice() {
        return Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Factory::choice);
    }

    /**
     * Constructs the expression l|r
     */
    public static Parallel parallel(String l, CCSExpression r) {
        return new Parallel(var(l), r);
    }

    /**
     * Constructs the expression l|r
     */
    public static Parallel parallel(CCSExpression l, String r) {
        return new Parallel(l, var(r));
    }

    /**
     * Constructs the expression l|r
     */
    public static Parallel parallel(String l, String r) {
        return new Parallel(var(l), var(r));
    }

    /**
     * Constructs the big parallelisation of all expressions given
     */
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

    /**
     * Constructs the big parallelisation of all expressions given
     */
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

    /**
     * Collects a stream into a big parallelisation
     */
    public static Collector<CCSExpression, ?, CCSExpression> toParallel() {
        return Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Factory::parallel);
    }

    /**
     * Constructs the expression l\r
     */
    public static Restrict restrict(CCSExpression l, FilterSet r) {
        return new Restrict(l, r);
    }

    /**
     * Constructs the expression l\r
     */
    public static Restrict restrict(String var, FilterSet r) {
        return new Restrict(var(var), r);
    }

    /**
     * Constructs the expression a.e
     */
    public static Prefix prefix(Action a, CCSExpression e) {
        return new Prefix(a, e);
    }

    /**
     * Constructs the expression a.e
     */
    public static Prefix prefix(Action a, String var) {
        return new Prefix(a, var(var));
    }

    /**
     * Constructs the expression 0
     */
    public static Stop stop() {
        return Stop.instance();
    }

    /**
     * Constructs the expression that is the recursion variable s
     */
    public static Variable var(String s) {
        return new Variable(s);
    }

    /**
     * Constructs τ, the internal action
     */
    public static Action.InternalAction tau() {
        return Action.tau();
    }

    /**
     * Constructs c!, the action "sending" on channel c
     */
    public static Action.SendingAction sending(Channel c) {
        return Action.sending(c);
    }

    /**
     * Constructs s!, the action "sending" on channel s
     */
    public static Action.SendingAction sending(String s) {
        return sending(of(s));
    }

    /**
     * Constructs c?, the action "receiving" on channel c
     */
    public static Action.ReceivingAction receiving(Channel c) {
        return Action.receiving(c);
    }

    /**
     * Constructs c?, the action "receiving" on channel s
     */
    public static Action.ReceivingAction receiving(String s) {
        return receiving(of(s));
    }

    /**
     * Constructs the channel 'name'
     */
    public static Channel of(String name) {
        return new Channel(name);
    }

    /**
     * Constructs a filter set that excludes the given channels
     */
    public static FilterSet excluding(Channel... c) {
        return new FilterSet(true, Set.of(c));
    }

    /**
     * Constructs a filter set that excludes the given channels
     */
    public static FilterSet excluding(String... c) {
        return new FilterSet(true, Arrays.stream(c).map(Factory::of).collect(Collectors.toUnmodifiableSet()));
    }

    /**
     * Constructs a filter set that only includes the given channels
     */
    public static FilterSet including(Channel... c) {
        return new FilterSet(false, Set.of(c));
    }

    /**
     * Constructs a filter set that only includes the given channels
     */
    public static FilterSet including(String... c) {
        return new FilterSet(false, Arrays.stream(c).map(Factory::of).collect(Collectors.toUnmodifiableSet()));
    }

}
