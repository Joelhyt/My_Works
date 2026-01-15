import random
import matplotlib.pyplot as plt


#---------------------
# Deck 
#---------------------
BASE_DECK = (
    ["Land"] * 8 +
    ["Other"] * 25 +
    ["Land"] * 8 +
    ["Other"] * 23 +
    ["Land"] * 8
)

# Lands are distributed a bit along the deck so lower amount...
# of shuffle iterations represent a bit more realistic result
# Doesn't really matter with high shuffle amounts


#---------------------
# Shuffle functions
#---------------------
def riffle_shuffle(deck):
    cut = random.randint(len(deck) // 3, 2 * len(deck) // 3)
    left = deck[:cut]
    right = deck[cut:]

    shuffled = []
    while left or right:
        if left and (not right or random.random() < 0.5):
            shuffled.append(left.pop(0))
        if right and (not left or random.random() < 0.5):
            shuffled.append(right.pop(0))
    return shuffled


def overhand_shuffle(deck):
    shuffled = []
    while deck:
        chunk_size = random.randint(1, 5)
        chunk = deck[:chunk_size]
        deck = deck[chunk_size:]
        shuffled = chunk + shuffled
    return shuffled


def cut_shuffle(deck):
    cut = random.randint(1, len(deck) - 1)
    return deck[cut:] + deck[:cut]


def human_shuffle(deck, iterations):
    for _ in range(iterations):
        deck = random.choice([
            riffle_shuffle,
            overhand_shuffle,
            cut_shuffle
        ])(deck)
    return deck

#---------------------
# Simulation
#---------------------

NUM_SIMULATIONS = 10000
lands_in_hand = []

for _ in range(NUM_SIMULATIONS):
    deck = BASE_DECK.copy()      # Decks won't be perfectly shuffled after game so fresh "deck" for new simulation
    deck = human_shuffle(deck, 25)  # Lower than 15 iterations could cause unrealistic distribution of lands in the deck.

    hand = deck[:7]
    lands_in_hand.append(hand.count("Land"))

#---------------------
# Visualization
#---------------------

print("Deck size of " + str(len(BASE_DECK)) + " with " + str(BASE_DECK.count("Land")) + " lands.")


plt.figure(figsize=(8, 5))
plt.hist(
    lands_in_hand,
    bins=range(0, 9),
    align="left",
    rwidth=0.8
)

plt.xlabel("Number of Lands in Opening Hand (7 cards)")
plt.ylabel("Frequency")
plt.title("Opening Hand Land Distribution")
plt.xticks(range(0, 8))

plt.show()
