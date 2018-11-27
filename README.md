# XYPointerAnalysis

> Authors: Dexin Liu && Peiyi Sun

## Intro

This is a pointer analysis framework for Java, based on *Soot* analysis framework.  Actually, it is the first project for course *Software Analysis in Fall 2018*.

## Example

```java
//The input program:
public static void main(String[] args) {
    Benchmark.alloc(1); //标记分配点,没有标记的默认编号为0 
    A a = new A(); 
    Benchmark.alloc(2); 
    A b = new A(); 
    Benchmark.alloc(3); 
    A c = new A(); 
    if (args.length>1) 
        a=b; 
    Benchmark.test(1, a); //标记测试点编号和被测变量 
    Benchmark.test(2, c); 
} 

-----------
//The output should be:
1: 1 2 
2: 3
```

## How to use

In *intelliJ*,  set the run configurations to be 

```
path_of_rt.jar path_of_input_class class_name
```

For example:

```sh
C:\Users\spy\IdeaProjects\XYPointerAnalysis\lib\ C:\Users\spy\IdeaProjects\XYPointerAnalysis\out\production\XYPointerAnalysis\ FieldSensitivity
```

