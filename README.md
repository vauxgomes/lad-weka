![](img/lad-weka-min.png)

### About LAD
Logical Analysis of Data (LAD) is a rule-based machine learning algorithm based on ideas from Optimization and Boolean Function Theory. The LAD methodology was originally conceived by Peter L. Hammer, from Rutgers University, and has been described and developed in a number of papers since the late 80's. It has also been applied to classification problems arising in areas such as Medicine, Economics, and Bioinformatics. A list with representative publications about LAD will be made available here shortly.

### About WEKA
WEKA is an open-source environment for data analysis and machine learning, developed by the Machine Learning group of the University of Waikato, New Zealand. WEKA is the de facto standard for comparing machine learning algorithms and the tool of choice for many data mining and machine learning practitioners. The WEKA package provides access to a range of classification algorithms, including state-of-the-art implementations of SVM, Neural Networks, and Random Forests.

### Project Goals
The goal of the LAD-WEKA project is to provide a reference implementation of LAD, which is free, portable, and does not depend on third-party software (such as linear and integer programming solvers). The fact that LAD-WEKA is implemented as a WEKA Classifier allows one to easily run experiments comparing LAD with other classification algorithms available in the WEKA package.

#### Related publications

**An Implementation of Logical Analysis of Data**. Boros, E., P.L. Hammer, T. Ibaraki, A. Kogan, E. Mayoraz, I. Muchnik. IEEE Transactions on Knowledge and Data Engineering, vol 12(2), 292-306, 2000

**Maximum Patterns in Datasets**. Bonates, T.O., P.L. Hammer, A. Kogan. Discrete Applied Mathematics, vol. 156(6), 846-861, 2008.

**Classificação Supervisionada de Dados via Otimização e Funções Booleanas**. Gomes, V.S.D., T. O. Bonates. Anais do II Workshop Técnico-Científico de Computação, p.21-27, Mossoró, RN, Brazil, 2011.

## Download and Execution
A `jar` file is current placed [here](dist/lad-weka.jar). One may simple execute the downloaded file either by double clicking or using the following command:

```sh
$ java -jar lad-weka.jar
```

WEKA supports full CLI interaction. The following example calls LAD classifier with UCI's `vote` dataset.

```sh
$ java -classpath weka.jar weka.classifiers.rules.LAD -t vote.arff -T vote.arff
```

#### LAD options
For the full LAD's list of options use the following command:
```sh
$ java -classpath lad-weka.jar weka.classifiers.rules.LAD -help
```
