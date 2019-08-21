---
title: "Your In Game Wallet"
category: basics

---
Every CrownQuest wallet is a Cryptocurrency address. 
You can see your balance checking the in-game HUD ur using the command ```/wallet```.

In the Crown server, you can deposit CRW in the address shown when you join the server, and it will, after several minutes of processing, show the deposited Crown in your balance. You can spend that money [claiming land](/wiki/land.html), or sending money to other players.  

In CrownQuest, Crown is measured in Crws, that's 1/1,000,000th of a Crown (1CRW)

/transfer command
------------------
Usage: /transfer \<amount\> \<address\>

This command will transfer the specified amount from your wallet to the specified wallet address.

Note that transferring Crown to a wallet that isn't managed by CrownQuest will result in a miner's fee. Miner's fees can vary, but they are typically a couple hundred crws. To keep players from abusing this command by transferring amounts that are too small, the minimum amount of crws a player can transfer to an external wallet is 2000 crws. 

There are cases where fees are abnormally high and after fees, the account balance will be negative.

Example:
````
/transfer 10000 1BiWdDaHChaKTcNXWs3nxPRcxb6CHM5h2u
````

/send command
------------------
Usage: /send \<amount\> \<player\>

This command will transfer the specified amount from your wallet to the specified player's wallet.

Using /send to transfer Crown to another player allows CrownQuest to manage the transaction locally. In this situation, there will be no miner's fees, and you can send or receive any amount of Crown.

Example:
````
/send 1 Xeyler
````


/wallet command
-------------------
This command will display your Crown Address, your balance and a link to your wallet on Blockcypher. Although, since player wallets are HD in order to avoid miner fees, the amount displayed by Blockcypher will not be accurate.