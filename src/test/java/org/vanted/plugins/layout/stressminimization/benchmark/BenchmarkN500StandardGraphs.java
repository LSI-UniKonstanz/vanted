package org.vanted.plugins.layout.stressminimization.benchmark;

public class BenchmarkN500StandardGraphs extends BenchmarkStandardGraphs {

    public static void main(String[] args) {

        BenchmarkN500StandardGraphs b = new BenchmarkN500StandardGraphs();
        b.benchmark();

    }

    @Override
    protected int getN() {
        return 500;
    }

}
