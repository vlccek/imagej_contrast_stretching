# Plugin for ImageJ for contras changing

# Used function for transformation:

1) Power function

$$f(x) = x^\lambda$$
2) square function
$$f(x) = x^{\frac{1}{\lambda}}$$
3) Natural log (ln)

$$f(x) = \frac{ln(x)}{ln(255)\cdot\lambda}$$
4) Sigmoid 

$$f(x) = 1 + \frac{\frac{sin(\lambda\cdot x - 0.5)}{ sin(\frac{\lambda }{2})}}{2}$$
5) Sigmoid tan

$$f(x)=1+ \frac{tan(\lambda \cdot (x - 1))}{tan(\frac{lambda}{2}) * 2}
$$
4) sigmoid nn.

$$
f(x) = \frac{1}{1 + e^{-2 \cdot \ln(255) \cdot \left(\frac{x}{255} - 0.5\right)}}
$$

5) Hyperbolic tan (tanh)


$$
f(x) = \frac{tanh(6x\lambda-3\lambda)+1}{2} 
$$

All of the function can be customized by $\lambda$ parametr.