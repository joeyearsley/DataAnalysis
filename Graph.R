##############################
# Import necessary libraries #
##############################

library(rmongodb)
library(plyr)
library(ggplot2)
library(scales)
library(dtw)

citation("dtw")
citation("scales")
citation("rmongodb")
citation("ggplot2")
citation("plyr")

args <- commandArgs(trailingOnly = TRUE)
############################################
# Functions to save on rewriting code.     #
# To add both tables into one data frame.  #
############################################

###########################################################
# addData to DataFrame, function to save writing it twice #
###########################################################

addData <- function(cursor,type,df,round) {
  while (mongo.cursor.next(cursor)) {
    # Iterate and grab the next record
    tmp = mongo.bson.to.list(mongo.cursor.value(cursor))
    # Make it a dataframe
    tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = F)
    # Add new type to allow grouping
    tmp.df$type = type
    # Round values to show, if round ! 0
    tmp.df$value = format(round(as.numeric(tmp.df$value), round), nsmall = round,scientific = FALSE)
    # Strip out the ID
    tmp.df <- tmp.df[c("subject","value","type")]
    # Bind to the master dataframe
    df = rbind.fill(df, tmp.df)
  }
  return(df)
}

######################################################
# Save plot function - NOT IN USE EASIER WITH GGSAVE #
######################################################

savePlot <- function(myPlot,type,name) {
  if(type == "pdf"){
    c = paste(name,".pdf", sep = "")
    pdf(c)
  } else if (type == "jpeg"){
    c = paste(name,".jpeg", sep = "")
    jpeg(c)
  } else if (type == "png"){
    c = paste(name,".png", sep = "")
    png(c)
  } else if (type == "ps"){
    c = paste(name,".eps", sep = "")
    postscript(c, width = 12, height = 17, horizontal = FALSE, onefile = FALSE, paper = "special",)
  }
  print(myPlot)
  return(dev.off())
}

#########################
# Set Working Directory #
#########################

setwd("/Users/josephyearsley/Documents/University/Dissertation/R")

# Connect to DB
mongo = mongo.create(host = "localhost")
mongo.is.connected(mongo)

graph <- function(fileName, fileType, graphName, graphType, rounding){
  ###############################################################
  # Setup DB calls and data structure for storing returned data #
  ###############################################################
  
  # Create the empty data frame
  df = data.frame(stringsAsFactors = FALSE)
  
  if(graphType != "dtw"){
    # Get all self similarities
    cursorSelf = mongo.find(mongo, ns = "Dissertation.selfCosine")
     df = addData(cursorSelf,"Self",df,rounding)
    # Get all diff Similarites
    cursorDiff = mongo.find(mongo, ns = "Dissertation.diffCosine")
    df = addData(cursorDiff,"Cross",df,rounding)
  }else{
    # Get all self similarities
    cursorSelf = mongo.find(mongo, ns = "Dissertation.selfDTW")
    df = addData(cursorSelf,"Self",df,rounding)
    # Get all diff Similarites
    cursorDiff = mongo.find(mongo, ns = "Dissertation.diffDTW")
    df = addData(cursorDiff,"Cross",df,rounding)
  }
  ############################################################
  # Create GGplot with values on the y-axis, color upon type #
  ############################################################
  
  
  graph <- ggplot(data=df,aes(x = subject, y = value, fill = type)) 
  
  # Set grouped data side by side, and set the title
  graph <- graph +  geom_bar(position="dodge", stat = "identity") +  ggtitle(graphName)
  
  # Set legend name and axis labels
  graph <- graph +  scale_fill_discrete(name="Type Of\nSimilarity") + ylab("Similarity (%)") + xlab("Subject")
  #graph <- graph + scale_y_continuous(labels=comma) 
  # Ensure all open devices are closed
  graphics.off()
  
  # Print out plot
  print(graph)
  
  name = paste(fileName, fileType, sep = ".")
  
  # Save file - GGSAVE EASIER THAN MY OWN SAVE FUNCTION
  ggsave(filename=name, plot = graph)
  
}
graph("test","eps","test","dtw",7)

options(scipen=999)
graph(args[2], args[3], args[4], args[5], as.numeric(args[6]))
