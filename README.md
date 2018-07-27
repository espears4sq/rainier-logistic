
# rainier-logistic

An example of probabilistic inference on synthetic data
with the [rainier](https://github.com/stripe/rainier) library.

Run `sbt compile` and `sbt run` to generate a summary of a toy
logistic regression demonstrating calculation of posterior mean,
standard deviation, and 95% credible intervals for an intercept
term and a slope term, such as:

```
[info] Running logistic
          Regression Summary
------------------------------------
          ||       b0   ||        b1
------------------------------------
mean      ||   00.029   ||    00.298
std. dev. ||   00.069   ||    00.026
5th pctl  ||   -0.085   ||    00.256
95th pctl ||   00.142   ||    00.142
------------------------------------
```

The program also writes rwo image files into the /images folder:
`traceplots.png` and `pairs.png`, which implement monte carlo
trace plots and parameter correlation (pairs) plots, like below:

![traceplot][trace]

![pairsplot][pairs]

## citations

This code is modified from the blog post below, retrieved on
07/27/2018.

- [Monadic probabilistic programming in Scala with Rainier][1]

[1]: https://darrenjw.wordpress.com/2018/06/01/monadic-probabilistic-programming-in-scala-with-rainier/
[trace]: images/static/traceplots.png
[pairs]: images/static/pairs.png