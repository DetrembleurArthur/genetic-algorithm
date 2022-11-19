

path <- "D:\\Users\\mb624\\Documents\\GitHub\\genetic-algorithm\\population.csv"
data.interne <- read.table(path, header=T, sep=";", dec=".")


i <- 1
while(i <= 1)
{
  genome.1 <- data.interne[data.interne$genome==i,-c(2)]
  par(mfrow=c(1,2))
  plot(genome.1, main=paste("Genome: ", as.character(i)))
  summary(genome.1)
  plot(sort(genome.1$fitness))
  i <- i + 1
}
estimfitnessIdeale <- max(data.interne$fitness)
