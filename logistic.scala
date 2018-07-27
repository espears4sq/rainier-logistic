import com.cibo.evilplot.geometry.Extent
import com.stripe.rainier.compute._
import com.stripe.rainier.core._
import com.stripe.rainier.plot.EvilTracePlot._
import com.stripe.rainier.sampler._


object logistic extends App {

  // Helper function to map synthetically generated data into
  // training labels of a logistic regression.
  def logistic_fn(x: Double): Double = {
    1.0 / (1.0 + math.exp(-x))
  }

  // Helper function to extract the given percetile value from a
  // list, used to get posterior percentiles.
  def percentile(pctl: Double, values: List[Double]): Double = {
    val sorted = values.sorted
    val k = math.ceil((values.length - 1) * (pctl / 100.0)).toInt
    sorted(k)
  }

  // Parameters for creating synthetic data
  val r = new scala.util.Random(0)
  val N = 1000
  val beta0_true = 0.1
  val beta1_true = 0.3

  // Create synthetic logistic regression data set.
  val x = (1 to N) map { i => 3.0 * r.nextGaussian }
  val theta = x map { xi => beta0_true + beta1_true * xi }
  val y = theta map logistic_fn map {p_i => (r.nextDouble < p_i)}

  // Define posterior distribution over parameters.
  val model = for {
    beta0 <- Normal(0, 5).param  // Intercept term with Gaussian prior
    beta1 <- Normal(0, 5).param  // Slope coefficient with Gaussian prior
    _ <- Predictor.from{x: Double => {
      // Specify the logistic model probability.
      val theta = beta0 + beta1 * x
      val p = Real(1.0) / (Real(1.0) + (Real(0.0) - theta).exp)
      Categorical.boolean(p)  // Result expression is a Bernoulli random variable
                              // with parameter p so the likelihood implicitly
			      // becomes the product of these terms across the
			      // data set provided to `fit`, and the Rainier model
			      // knows how to convert this to a log likelihood and
			      // calculate gradients for model training.
    }}.fit(x zip y)
  } yield Map("b0" -> beta0, "b1" -> beta1)  // Random-valued object using posterior
                                             // distributions when asked to sample
					     // any variables from the above block.

  // At the end of the block above, 'model' is a _random_ variable whose values
  // are Map objects containing a random draw of beta0 and beta1 from their
  // implied posterior distributions.


  // Set some parameters for materializing real random draws from 'model'.
  implicit val rng = ScalaRNG(3)
  val burn_in = 10000 // MCMC burn in iterations
  val its = 10000     // desired number of samples
  val thin = 5        // MCMC thinning parameter. thin * its is effective iterations.
  val leap_steps = 5  // Tuning parameter for the underlying NUTS algorithm.


  // Use the built-in MCMC algorithm from Rainier to sample 10,000 pairs
  // of (beta0, beta1) from the posterior distribution implied by the
  // training data.
  val coefficient_samples = model.sample(
    HMC(leap_steps),  // HMC-NUTS sampler with provided leap frog step parameter
    burn_in,          // iterations to discard from start-up
    its * thin,       // effective total iterations
    thin              // keep every `thin`-th sample, reducing to `its` samples.
  )


  // Store the history of separate coefficient samples by
  // extracting from each of the sampled Map values.
  val b0s = coefficient_samples map {x => x("b0")}
  val b1s = coefficient_samples map {x => x("b1")}


  // Calculate some summary statistics describing the coefficients:
  val b0_mean = b0s.sum / its
  val b1_mean = b1s.sum / its
  val b0_std = math.sqrt((b0s map {b => math.pow(b, 2)}).sum / its - math.pow(b0_mean, 2))
  val b1_std = math.sqrt((b1s map {b => math.pow(b, 2)}).sum / its - math.pow(b1_mean, 2))
  val b0_2p5 = percentile(2.5, b0s)
  val b1_2p5 = percentile(2.5, b1s)
  val b0_50 = percentile(50, b0s)
  val b1_50 = percentile(50, b1s)
  val b0_97p5 = percentile(97.5, b0s)
  val b1_97p5 = percentile(97.5, b0s)


  // Print a diagnostic summary
  println("          Regression Summary")
  println("------------------------------------")
  println("          ||       b0   ||        b1")
  println("------------------------------------")
  println(f"mean      ||   ${b0_mean}%06.3f   ||    ${b1_mean}%06.3f   ")
  println(f"std. dev. ||   ${b0_std}%06.3f   ||    ${b1_std}%06.3f   ")
  println(f"2.5 pctl  ||   ${b0_2p5}%06.3f   ||    ${b1_2p5}%06.3f   ")
  println(f"50 pctl   ||   ${b0_50}%06.3f   ||    ${b1_50}%06.3f   ")
  println(f"97.5 pctl ||   ${b0_97p5}%06.3f   ||    ${b1_97p5}%06.3f   ")
  println("------------------------------------")


  // Produce plotting artifacts summarizing the distribution of posterior
  // parameters.
  val tr = Map("b0" -> beta0_true, "b1" -> beta1_true)
  render(traces(coefficient_samples, truth=tr), "images/traceplots.png", Extent(1200, 1000))
  render(pairs(coefficient_samples, truth=tr), "images/pairs.png")
}