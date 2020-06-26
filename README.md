# Portfolio-Investment-Tracker
A tool which allows end-users to track the value of their investments over the past 3 years.
## Prerequisite(s)
+ SQL-Lite Jar
## Overview
This is a single-page Java Swing Application split into 3 sections. The top-left section requires the user to supply a date, company name (whilst users can only choose from a list of several companies, such as Google or Amazon, the application can be modified to accomodate any company, so long as there is also information regarding its stocks) and quantity (representing the number of shares bought). After entering the aforementioned data, a graph is displayed - on the bottom-left section of the application - showing that particular stock's value in accordance with [IExtrading stocks](https://iextrading.com/apps/stocks). The top-right section displays the inventory value, which is the sum of the quantity of each stock multiplied by its respective value across all companies; in other words, the total value of all investments you have made.
