package utils;


import cern.jet.random.Exponential;
import cern.jet.random.Gamma;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import engine.exception.ProcessValidationException;
import model.xsd.DistributionHistogramBin;
import model.xsd.DistributionInfo;
import model.xsd.DistributionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class RandomGenerator {
    private RandomEngine engine;
    private Normal normal;
    private Uniform uniform;
    private Exponential exponential;
    private Gamma gamma;

    public RandomGenerator() {
        int seed = (int)(Math.random() * 2.147483647E9D);
        this.engine = new MersenneTwister(seed);
    }

    private void logSeed(int seed) {
        File f = new File("output/lastseed.txt");

        try {
            FileWriter fw = new FileWriter(f);
            fw.write(seed + "");
            fw.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    public double normal(double mean, double stdDev) {
        if (this.normal == null) {
            this.normal = new Normal(0.0D, 0.0D, this.engine);
        }

        return this.normal.nextDouble(mean, stdDev);
    }

    public double exponential(double lambda) {
        if (this.exponential == null) {
            this.exponential = new Exponential(0.0D, this.engine);
        }

        return this.exponential.nextDouble(lambda);
    }

    public double uniform(double min, double max) {
        if (this.uniform == null) {
            this.uniform = new Uniform(this.engine);
        }

        return this.uniform.nextDoubleFromTo(min, max);
    }

    public double lognormal(Double mean, Double variance) {
        double z = this.normal(0.0D, 1.0D);
        Double msq = mean * mean;
        double a = Math.log(msq / Math.sqrt(variance + msq));
        double b = Math.sqrt(Math.log(1.0D + variance / msq));
        return Math.exp(a + b * z);
    }

    public double triangular(Double mode, Double minA, Double maxB) {
        double u = this.uniform(0.0D, 0.999999999D);
        double fc = (mode - minA) / (maxB - minA);
        return u < fc ? minA + Math.sqrt(u * (maxB - minA) * (mode - minA)) : maxB - Math.sqrt((1.0D - u) * (maxB - minA) * (maxB - mode));
    }

    public double gamma(Double mean, Double variance) {
        if (this.gamma == null) {
            this.gamma = new Gamma(1.0D, 1.0D, this.engine);
        }

        double alpha = mean * mean / variance;
        double lambda = 1.0D / (variance / mean);
        return this.gamma.nextDouble(alpha, lambda);
    }

    public double fromDistributionInfo(DistributionInfo di) throws ProcessValidationException {
        if (di == null) {
            return 0.0D;
        } else {
            switch(di.getType()) {
                case FIXED:
                    if (di.getMean() != null && !di.getMean().isNaN()) {
                        return di.getMean();
                    }

                    throw new ProcessValidationException("Mean parameter is required for fixed distribution");
                case UNIFORM:
                    if (di.getArg1() != null && !di.getArg1().isNaN() && di.getArg2() != null && !di.getArg2().isNaN()) {
                        return this.uniform(di.getArg1(), di.getArg2());
                    }

                    throw new ProcessValidationException("Minimum and maximum parameters are required for uniform distribution");
                case NORMAL:
                    if (di.getMean() != null && !di.getMean().isNaN() && di.getArg1() != null && !di.getArg1().isNaN()) {
                        return this.normal(di.getMean(), di.getArg1());
                    }

                    throw new ProcessValidationException("Mean and standard deviation parameters are required for normal distribution");
                case EXPONENTIAL:
                    if (di.getArg1() != null && !di.getArg1().isNaN() && di.getArg1() != 0.0D) {
                        return this.exponential(1.0D / di.getArg1());
                    }

                    return 0.0D;
                case GAMMA:
                    if (di.getMean() != null && !di.getMean().isNaN() && di.getArg1() != null && !di.getArg1().isNaN()) {
                        if (di.getMean() > 0.0D && di.getArg1() >= 0.0D) {
                            return this.gamma(di.getMean(), di.getArg1());
                        }

                        throw new ProcessValidationException("Invalid parameter values for gamma distribution. Following condition not met: mean > 0 and variance >= 0");
                    }

                    throw new ProcessValidationException("Mean and variance parameters are required for gamma distribution");
                case TRIANGULAR:
                    if (di.getMean() != null && !di.getMean().isNaN() && di.getArg1() != null && !di.getArg1().isNaN() && di.getArg2() != null && !di.getArg2().isNaN()) {
                        if (di.getArg1() < di.getArg2() && di.getArg1() <= di.getMean() && di.getMean() <= di.getArg2()) {
                            return this.triangular(di.getMean(), di.getArg1(), di.getArg2());
                        }

                        throw new ProcessValidationException("Invalid parameter values for triangular distribution. Required a < b, a <= mode <= b");
                    }

                    throw new ProcessValidationException("Mode, minimum at a and maximum at be parameters are required for triangular distribution");
                case LOGNORMAL:
                    if (di.getMean() != null && !di.getMean().isNaN() && di.getArg1() != null && !di.getArg1().isNaN()) {
                        if (di.getMean() > 0.0D && di.getArg1() >= 0.0D) {
                            return this.lognormal(di.getMean(), di.getArg1());
                        }

                        throw new ProcessValidationException("Following condition not met for log-normal distribution: mean > 0 and variance >= 0");
                    }

                    throw new ProcessValidationException("Mean and variance parameters are required for log-normal distribution");
                case HISTOGRAM:
                    return this.getFromHistogramDistribution(di);
                default:
                    throw new IllegalArgumentException("Unknown distribution type: " + di.getType());
            }
        }
    }

    public double random() {
        return this.uniform(0.0D, 1.0D);
    }

    private double getFromHistogramDistribution(DistributionInfo di) throws ProcessValidationException {
        if (di.getType() != DistributionType.HISTOGRAM) {
            throw new IllegalArgumentException("Invalid argument, distribution type: " + di.getType());
        } else if (di.getHistogramDataBins() == null) {
            throw new ProcessValidationException("Invalid argument, no histogram bins defined for distribution");
        } else {
            List<DistributionHistogramBin> bins = di.getHistogramDataBins().getHistogramData();
            if (bins != null && !bins.isEmpty()) {
                double binProbability = this.uniform(0.0D, 99.999999D);
                double summedProbabilities = 0.0D;
                Iterator var7 = bins.iterator();

                DistributionHistogramBin binDefinition;
                do {
                    if (!var7.hasNext()) {
                        throw new ProcessValidationException("Failed to get value from histogram distribution. Please check histogram definition");
                    }

                    binDefinition = (DistributionHistogramBin)var7.next();
                    summedProbabilities += binDefinition.getProbability();
                } while(binProbability >= summedProbabilities);

                return this.fromDistributionInfo(binDefinition.getDistribution());
            } else {
                throw new ProcessValidationException("Invalid argument, no histogram bins defined for distribution");
            }
        }
    }
}
