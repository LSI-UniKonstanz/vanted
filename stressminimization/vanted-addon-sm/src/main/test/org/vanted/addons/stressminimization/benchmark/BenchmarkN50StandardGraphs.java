package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN50StandardGraphs extends BenchmarkStandardGraphs {

    public static void main(String[] args) {

        BenchmarkN50StandardGraphs b = new BenchmarkN50StandardGraphs();
        b.benchmark();

    }

    @Override
    protected int getN() {
        return 50;
    }

}
