import React from "react";

const Result = ({query, occurence, url, title}) => {
    // Create a regex pattern by joining the words to highlight with '|'
    const wordsArray = occurence.split(" ");

    // Split the text into an array of substrings
    //   const parts = text.split(pattern);
    // console.log("wordsArray", wordsArray);
    // console.log("query", query);
    const wordIndex = wordsArray.indexOf(query);

    const lower_index = wordIndex - 90 < 0 ? 0 : wordIndex - 90;
    const upper_index = wordIndex + 90 > wordsArray.length ? wordsArray.length : wordIndex + 90;

    const relevantText = wordsArray.slice(lower_index, upper_index)
    const split_query_lower = query.toLowerCase().split(" ");

    return (<div
        id="alert-additional-content-5"
        class="my-3 p-4  rounded-lg bg-slate-800 h-full"
        role="alert"
        style={{width: "800px"}}
    >
        <div class="flex items-left flex-col">
            <a
                href={`//${url}`}
                class="text-2xl font-medium text-blue-600 dark:text-blue-500 hover:underline"
            >
                {title}{" "}
            </a>
            <h3 class="text-sm text-gray-500">{url}</h3>

            {/* <a class="text-xl font-normal text-gray-800">Computer vision</a> */}
        </div>
        <div class="mt-2 mb-4 text-md text-gray-400 ">
            {relevantText.map((part, index) => {

                if (split_query_lower.some((word) =>
                    (part.toLowerCase().includes(word.toLowerCase()) || part.toLowerCase().startsWith(word)) && word.length > 3)
                )
                    return <strong style={{color: "#c50e1f"}} key={index}>{part}{" "}</strong>;
                else return part + " ";
            })}
        </div>
    </div>);
};

export default Result;
