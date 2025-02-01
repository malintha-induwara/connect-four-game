# Connect Four with Monte Carlo Algorithm.

* Connect 4 game made using JavaFX and Monte Carlo Tree Search Algorithem As AI.

### Create Game

![5](https://github.com/malintha-induwara/connect-four-game/assets/60071404/0c648510-9e3a-47c9-b7c9-bc5a405721f3)
### Game Progression
![2](https://github.com/malintha-induwara/connect-four-game/assets/60071404/1b969912-d73a-4ecd-8620-5642e4fa9a1a)

![3](https://github.com/malintha-induwara/connect-four-game/assets/60071404/87b008d2-2db9-4402-a7b8-3d74c3bf2163)

![4](https://github.com/malintha-induwara/connect-four-game/assets/60071404/c527d249-fdee-465a-82a3-4adaae67df89)

### AI Implementation & LLM Testing
This project has also been used as a benchmark for testing the ability of large language models (LLMs) to implement Monte Carlo Tree Search (MCTS). Since GPT-3, all (free-tier) models from OpenAI have struggled to produce a working implementation of MCTS, including other models such as Claude 3.5 (Anthropic) and DeepSeek-R1 (DeepSeekAI). However, GPT-4o Mini(Low) successfully implemented the algorithm, making it the first in this series of models to do so.

You can find the LLM produced code inside the folder **`src/main/java/lk/ijse/dep/service/llm`**.

The implementation inside the `AiPlayer` class is the original implementation and was developed entirely without any help from LLMs.

### How to use this repo
* Open the pom.xml via IntelliJ IDEA
* Make sure to the open it as a project, if prompt
* Reload the pom.xml file via Maven Tool Window
* Create a run configuration for Maven via Run > Edit Configuration
* Add javafx:run as the Run command
  
That's it.
