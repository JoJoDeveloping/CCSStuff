package de.jojomodding.np;

import de.jojomodding.np.algo.CCSTransitionDerivation;
import de.jojomodding.np.algo.Minimization;
import de.jojomodding.np.ccs.expr.Binding;
import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.ccs.parse.Parser;
import de.jojomodding.np.lts.LTS;
import de.jojomodding.np.util.Pair;

import java.util.Map;
import java.util.Set;

import static de.jojomodding.np.Factory.*;

public class Main {

    public static void main(String[] args) {
        CCSExpression example = choice(sending("a").then(stop()), parallel("X", restrict(tau().then(stop()), excluding("cc"))));
        System.out.println(example);
        CCSTransitionDerivation cd = new CCSTransitionDerivation(new Binding(Map.of(
                "X", sending("a").then("Y"),
                "Y", receiving("a").then("X"),
                "Z", parallel("X", "Y"),
                "A1", var("A2"),
                "A2", var("A1")
        )));
        LTS<CCSExpression> lts = (cd.getReachableLTS(choice("Z", "A1")));
        System.out.println(lts);
        System.out.println(Minimization.minimizeBisimilarity(lts));
        System.out.println(Minimization.minimizeWeakBisimilarity(lts));
        System.out.println(Minimization.minimizeObservationCongruence(lts));
        System.out.println(lts.randomWalk(true));
        System.out.println();


        String raw = "Match := strike?. MatchOnFire\n"
                     + "MatchOnFire := light!. MatchOnFire + extinguish!.0\n"
                     + "TwoFireCracker := light?. (bang!. 0 | bang!. 0)\n"
                     + "\n"
                     + "(Match | TwoFireCracker) \\ {light}";
        Pair<Binding, CCSExpression> k = Parser.parse(raw);
        CCSTransitionDerivation cd1 = new CCSTransitionDerivation(k.first());
        LTS<CCSExpression> mlts = cd1.getReachableLTS(k.second());
        System.out.println(k);
        System.out.println(mlts);
        LTS<Set<CCSExpression>> min = Minimization.minimizeWeakBisimilarity(mlts);
        System.out.println(min);
        System.out.println(min.toPseuco());
        System.out.println(mlts.randomWalk(false));

        System.out.println();
        System.out.println();
        Pair<Binding, CCSExpression> kk = Parser.parse(
                "Scientist := getmail? . MailWritingScientist + zoom? . (Scientist + meet! . Scientist)\n"
                + "MailWritingScientist := moan! . MailWritingScientist + sendmail! . Scientist + zoom? . (MailWritingScientist + meet! . MailWritingScientist)\n"
                + "Scientists := Scientist | Scientist | Scientist\n"
                + "Server := getmail! . Server + sendmail? . Server + i . reboot? . Server\n"
                + "Administrator := music? . Administrator + reboot! . Administrator\n"
                + "Professor := read! . Professor + zoom! . zoom! . zoom! . \n"
                + "            (scream! . Professor + meet? . \n"
                + "                    (scream! . Professor + meet? .\n"
                + "                            (scream! . Professor + meet? . workharder! . Professor)))\n"
                + "(Scientists | Server | Administrator | Professor) \\ {reboot, getmail, sendmail, zoom, meet}");
        System.out.println(kk);
        CCSTransitionDerivation cr = new CCSTransitionDerivation(kk.first());
        LTS<CCSExpression> scientistLTS = cr.getReachableLTS(kk.second());
        System.out.println(scientistLTS.rename());
        LTS<Set<CCSExpression>> miniScientist = Minimization.minimizeObservationCongruence(scientistLTS);
        System.out.println(miniScientist);
    }

}
