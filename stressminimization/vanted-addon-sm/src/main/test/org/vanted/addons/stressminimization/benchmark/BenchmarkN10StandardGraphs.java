package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN10StandardGraphs extends BenchmarkStandardGraphs {

    public static void main(String[] args) {

        BenchmarkN10StandardGraphs b = new BenchmarkN10StandardGraphs();
        b.benchmark();

    }

    @Override
    protected int getN() {
        return 10;
    }

}
